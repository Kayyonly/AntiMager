const { Client, LocalAuth } = require("whatsapp-web.js");
const qrcode = require("qrcode-terminal");
const express = require("express");
const dotenv = require("dotenv");
const fs = require("fs");
const path = require("path");
const os = require("os");

dotenv.config();

const PORT = Number(process.env.PORT || 3000);
const BRIDGE_TOKEN = process.env.BRIDGE_TOKEN || "antimager-local";
const GROQ_API_KEY = process.env.GROQ_API_KEY || "";
const GROQ_MODEL = process.env.GROQ_MODEL || "openai/gpt-oss-120b";
const DATA_DIR = path.join(__dirname, "data");
const STATE_FILE = path.join(DATA_DIR, "state.json");

fs.mkdirSync(DATA_DIR, { recursive: true });

function loadState() {
  try {
    return JSON.parse(fs.readFileSync(STATE_FILE, "utf8"));
  } catch {
    return { selfChatJid: null, conversation: null, tasks: [] };
  }
}

let state = loadState();

function saveState() {
  fs.writeFileSync(STATE_FILE, JSON.stringify(state, null, 2));
}

function localAddresses() {
  const result = [];
  for (const entries of Object.values(os.networkInterfaces())) {
    for (const info of entries || []) {
      if (info.family === "IPv4" && !info.internal) {
        result.push("http://" + info.address + ":" + PORT);
      }
    }
  }
  return result;
}

function requireToken(req, res, next) {
  if (req.header("X-AntiMager-Token") !== BRIDGE_TOKEN) {
    return res.status(401).json({ error: "unauthorized" });
  }
  next();
}

let whatsappReady = false;

const api = express();
api.use(express.json());

api.get("/health", (_req, res) => {
  res.json({
    ok: true,
    whatsappReady,
    pendingTasks: state.tasks.filter((task) => !task.ackedAt).length
  });
});

api.get("/api/tasks", requireToken, (_req, res) => {
  res.json({ tasks: state.tasks.filter((task) => !task.ackedAt) });
});

api.post("/api/tasks/:id/ack", requireToken, (req, res) => {
  const task = state.tasks.find((item) => item.id === req.params.id);
  if (!task) return res.status(404).json({ error: "task_not_found" });
  task.ackedAt = Date.now();
  saveState();
  res.json({ ok: true });
});

api.listen(PORT, "0.0.0.0", () => {
  console.log("\nAntiMager Bridge API aktif di port " + PORT);
  const addresses = localAddresses();
  if (addresses.length) {
    console.log("Masukkan URL ini ke AntiMager > Pengaturan > WhatsApp Bridge:");
    addresses.forEach((address) => console.log("  " + address));
  } else {
    console.log("Cek ipconfig lalu gunakan http://IP-LAPTOP:" + PORT);
  }
});

const client = new Client({
  authStrategy: new LocalAuth({ clientId: "antimager" }),
  puppeteer: {
    headless: true,
    args: ["--no-sandbox", "--disable-setuid-sandbox"]
  }
});

client.on("qr", (qr) => {
  console.log("\nScan QR WhatsApp:");
  qrcode.generate(qr, { small: true });
});

client.on("authenticated", () => console.log("WhatsApp authenticated."));
client.on("ready", () => {
  whatsappReady = true;
  console.log("WhatsApp ready.");
  console.log("Kirim di chat diri sendiri, contoh: + ada PR B Indo");
});
client.on("disconnected", (reason) => {
  whatsappReady = false;
  console.log("WhatsApp disconnected:", reason);
});

function nowJakartaText() {
  return new Intl.DateTimeFormat("id-ID", {
    timeZone: "Asia/Jakarta",
    dateStyle: "full",
    timeStyle: "long"
  }).format(new Date());
}

async function groqJson(messages) {
  if (!GROQ_API_KEY) throw new Error("GROQ_API_KEY belum diisi di .env");

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 12000);

  let response;
  try {
    response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
    method: "POST",
    headers: {
      Authorization: "Bearer " + GROQ_API_KEY,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      model: GROQ_MODEL,
      temperature: 0.1,
      reasoning_effort: "low",
      reasoning_format: "hidden",
      response_format: { type: "json_object" },
      messages
    }),
    signal: controller.signal
    });
  } finally {
    clearTimeout(timeout);
  }

  if (!response.ok) {
    const body = await response.text();
    throw new Error("Groq HTTP " + response.status + ": " + body.slice(0, 180));
  }

  const data = await response.json();
  const text = data.choices && data.choices[0] && data.choices[0].message && data.choices[0].message.content;
  if (!text) throw new Error("Groq mengembalikan respon kosong");
  return JSON.parse(text);
}

function parseSimpleIndonesianDeadline(text) {
  const value = String(text || "").toLowerCase().trim();
  const now = new Date();

  const timeMatch = value.match(/(?:jam\s*)?(\d{1,2})(?:[.:](\d{2}))?\s*(pagi|siang|sore|malam)?/);
  if (!timeMatch) return null;

  let hour = Number(timeMatch[1]);
  const minute = Number(timeMatch[2] || 0);
  const period = timeMatch[3];

  if (period === "siang" && hour < 12) hour += 12;
  if (period === "sore" && hour < 12) hour += 12;
  if (period === "malam" && hour < 12) hour += 12;
  if (period === "pagi" && hour === 12) hour = 0;

  let target = new Date(now);

  if (/\bbesok\b/.test(value)) {
    target.setDate(target.getDate() + 1);
  } else if (/\blusa\b/.test(value)) {
    target.setDate(target.getDate() + 2);
  } else {
    const dayNames = {
      minggu: 0, senin: 1, selasa: 2, rabu: 3,
      kamis: 4, jumat: 5, sabtu: 6
    };
    const found = Object.keys(dayNames).find((name) => value.includes(name));
    if (found) {
      const wanted = dayNames[found];
      let delta = (wanted - target.getDay() + 7) % 7;
      if (delta === 0) delta = 7;
      target.setDate(target.getDate() + delta);
    } else {
      return null;
    }
  }

  target.setHours(hour, minute, 0, 0);
  return target.getTime();
}

function fallbackTaskDraft(originalText) {
  const lower = originalText.toLowerCase();

  let subject = "Umum";
  if (/\b(mtk|matematika|math)\b/.test(lower)) subject = "Matematika";
  else if (/\b(ips)\b/.test(lower)) subject = "IPS";
  else if (/\b(ipa|sains|science)\b/.test(lower)) subject = "IPA";
  else if (/\b(b\.?\s?indo|bahasa indonesia|indo)\b/.test(lower)) subject = "Bahasa Indonesia";
  else if (/\b(inggris|bahasa inggris|english)\b/.test(lower)) subject = "Bahasa Inggris";
  else if (/\b(ppkn|pkn)\b/.test(lower)) subject = "PPKn";
  else if (/\b(agama)\b/.test(lower)) subject = "Agama";

  return {
    originalText,
    title: originalText.trim(),
    subject,
    description: originalText.trim(),
    deadlineEpochMillis: null,
    needsDeadline: true,
    estimatedMinutes: 30,
    priority: "MEDIUM",
    locationName: null,
    aiAdvice: ""
  };
}

async function parseTaskDraft(originalText, deadlineAnswer) {
  const context = deadlineAnswer
    ? 'Command awal: "' + originalText + '"\nJawaban deadline: "' + deadlineAnswer + '"'
    : 'Command: "' + originalText + '"';

  let parsed;
  try {
    console.log("Memproses task dengan Groq...");
    parsed = await groqJson([
    {
      role: "system",
      content:
        "Kamu parser task AntiMager. Ekstrak tugas bahasa Indonesia ke JSON. " +
        "Jangan mengarang deadline. Waktu referensi: " + nowJakartaText() + ". " +
        "Jika deadline belum disebut, deadlineIso harus null dan needsDeadline=true. " +
        "deadlineIso wajib ISO 8601 dengan offset +07:00. " +
        "subject gunakan nama mapel/kategori yang wajar. priority hanya HIGH/MEDIUM/LOW."
    },
    {
      role: "user",
      content:
        context +
        '\nBalas HANYA JSON: {"title":"","subject":"Umum","description":"","deadlineIso":null,' +
        '"needsDeadline":true,"estimatedMinutes":30,"priority":"MEDIUM","locationName":null,"aiAdvice":""}'
    }
  ]);
    console.log("Groq selesai memproses task.");
  } catch (error) {
    console.warn("Groq parser gagal, pakai fallback lokal:", error.message || error);
    if (!deadlineAnswer) {
      return fallbackTaskDraft(originalText);
    }

    const localDeadline = parseSimpleIndonesianDeadline(deadlineAnswer);
    if (localDeadline) {
      const fallback = fallbackTaskDraft(originalText);
      return {
        ...fallback,
        deadlineEpochMillis: localDeadline,
        needsDeadline: false
      };
    }

    throw error;
  }

  const deadlineMs = parsed.deadlineIso ? Date.parse(parsed.deadlineIso) : NaN;

  return {
    originalText,
    title: String(parsed.title || originalText).trim(),
    subject: String(parsed.subject || "Umum").trim() || "Umum",
    description: String(parsed.description || originalText).trim(),
    deadlineEpochMillis: Number.isFinite(deadlineMs) ? deadlineMs : null,
    needsDeadline: parsed.needsDeadline === true || !Number.isFinite(deadlineMs),
    estimatedMinutes: Math.min(240, Math.max(5, Number(parsed.estimatedMinutes || 30))),
    priority: ["HIGH", "MEDIUM", "LOW"].includes(String(parsed.priority || "").toUpperCase())
      ? String(parsed.priority).toUpperCase()
      : "MEDIUM",
    locationName: parsed.locationName ? String(parsed.locationName) : null,
    aiAdvice: String(parsed.aiAdvice || "").trim()
  };
}

function parseReminder(text) {
  const value = text.toLowerCase().trim();

  if (/^(ga ?usah|gak ?usah|tidak ?usah|tanpa reminder|tanpa pengingat|skip)$/.test(value)) return -1;
  if (/(pas deadline|saat deadline|tepat deadline)/.test(value)) return 0;
  if (/(malam sebelumnya|malam sebelum)/.test(value)) return 720;
  if (/^(sehari|satu hari)\s+(sebelumnya|sebelum)$/.test(value)) return 1440;

  const minutes = value.match(/(\d+)\s*(menit|mnt|min)/);
  if (minutes) return Math.max(0, Number(minutes[1]));

  const hours = value.match(/(\d+)\s*(jam|hour)/);
  if (hours) return Math.max(0, Number(hours[1]) * 60);

  const days = value.match(/(\d+)\s*(hari|day)/);
  if (days) return Math.max(0, Number(days[1]) * 1440);

  return null;
}

function formatDeadline(epoch) {
  return new Intl.DateTimeFormat("id-ID", {
    timeZone: "Asia/Jakarta",
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(epoch));
}

function reminderLabel(minutes) {
  if (minutes < 0) return "tanpa reminder";
  if (minutes === 0) return "pas deadline";
  if (minutes % 1440 === 0) return (minutes / 1440) + " hari sebelumnya";
  if (minutes % 60 === 0) return (minutes / 60) + " jam sebelumnya";
  return minutes + " menit sebelumnya";
}

async function sendToChat(chatJid, text) {
  if (chatJid) await client.sendMessage(chatJid, text);
}

async function getStableChatJid(msg) {
  try {
    const chat = await msg.getChat();
    return chat?.id?._serialized || msg.to;
  } catch {
    return msg.to;
  }
}

client.on("message_create", async (msg) => {
  try {
    const text = String(msg.body || "").trim();
    if (!text || !msg.fromMe || !whatsappReady) return;

    const ownPhoneJid = client.info && client.info.wid && client.info.wid._serialized;

    // Primary-phone messages come from the phone JID.
    // Messages generated by this Web session use the self LID, so ignore them.
    if (ownPhoneJid && msg.from !== ownPhoneJid) return;

    const currentChatJid = await getStableChatJid(msg);
    console.log("Pesan user di chat:", currentChatJid, "| raw to:", msg.to);

    if (text.startsWith("+")) {
      // Command boleh diketik dari chat mana pun yang kamu kirim sendiri.
      // Bot membalas dan melanjutkan percakapan di chat yang sama.
      const commandChatJid = currentChatJid;
      console.log("Command + diterima dari chat ->", commandChatJid);

      const command = text.slice(1).trim();
      if (!command) {
        await sendToChat(commandChatJid, "Tulis tugas setelah +. Contoh: + ada PR B Indo");
        return;
      }

      const draft = await parseTaskDraft(command);
      draft.sourceChatJid = commandChatJid;
      state.conversation = {
        chatJid: commandChatJid,
        step: draft.needsDeadline ? "deadline" : "reminder",
        draft
      };
      saveState();

      if (draft.needsDeadline) {
        await sendToChat(commandChatJid, 'Oke, "' + draft.title + '". Deadline-nya kapan?');
      } else {
        await sendToChat(
          commandChatJid,
          "Kebaca: " + draft.title +
          "\nDeadline: " + formatDeadline(draft.deadlineEpochMillis) +
          "\nMau diingatkan kapan? Contoh: 30 menit sebelumnya, 1 jam sebelumnya, pas deadline, atau gausah."
        );
      }
      return;
    }

    if (!state.conversation) return;

    if (currentChatJid !== state.conversation.chatJid) {
      console.log(
        "Pesan diabaikan karena beda chat. current:",
        currentChatJid,
        "| aktif:",
        state.conversation.chatJid
      );
      return;
    }

    console.log("Follow-up diterima. Step:", state.conversation.step, "| text:", text);

    if (state.conversation.step === "deadline") {
      const updated = await parseTaskDraft(state.conversation.draft.originalText, text);

      if (updated.needsDeadline || !updated.deadlineEpochMillis) {
        await sendToChat(state.conversation.chatJid, "Aku belum nangkep waktunya. Contoh: besok jam 8 pagi atau Jumat jam 16.00.");
        return;
      }

      const activeChatJid = state.conversation.chatJid;
      state.conversation = { chatJid: activeChatJid, step: "reminder", draft: updated };
      saveState();

      await sendToChat(
        activeChatJid,
        "Sip. Deadline " + formatDeadline(updated.deadlineEpochMillis) +
        ".\nMau diingatkan kapan? Contoh: 30 menit sebelumnya, 1 jam sebelumnya, pas deadline, atau gausah."
      );
      return;
    }

    if (state.conversation.step === "reminder") {
      const reminderMinutesBefore = parseReminder(text);

      if (reminderMinutesBefore === null) {
        await sendToChat(state.conversation.chatJid, "Pilih misalnya: 30 menit sebelumnya, 1 jam sebelumnya, pas deadline, atau gausah.");
        return;
      }

      const conversationChatJid = state.conversation.chatJid;
      const draft = state.conversation.draft;
      const task = {
        id: "wa_" + Date.now() + "_" + Math.random().toString(36).slice(2, 8),
        title: draft.title,
        subject: draft.subject,
        description: draft.description || draft.originalText,
        deadlineEpochMillis: draft.deadlineEpochMillis,
        estimatedMinutes: draft.estimatedMinutes,
        priority: draft.priority,
        isPersistent: true,
        locationName: draft.locationName,
        locationTrigger: draft.locationName ? "ENTER" : null,
        aiAdvice: draft.aiAdvice,
        reminderMinutesBefore,
        createdAt: Date.now(),
        ackedAt: null
      };

      state.tasks.push(task);
      state.conversation = null;
      saveState();

      await sendToChat(
        conversationChatJid,
        "*Tugas baru berhasil ditambahkan ke AntiMager*\n\n" +
        "Tugas: " + task.title +
        "\nKategori: " + task.subject +
        "\nDeadline: " + formatDeadline(task.deadlineEpochMillis) +
        "\nReminder: " + reminderLabel(task.reminderMinutesBefore) +
        "\nPrioritas: " + task.priority +
        "\n\nStatus: menunggu sinkronisasi ke app di HP."
      );

      console.log("Task WhatsApp dibuat:", task.title);
    }
  } catch (error) {
    console.error("WhatsApp flow error:", error);
    try {
      await sendToChat(state.conversation?.chatJid || msg.to, "Ada error saat membaca tugas. Coba lagi dengan command + baru.");
    } catch {}
  }
});

client.initialize().catch((error) => {
  console.error("WhatsApp init error:", error);
});
