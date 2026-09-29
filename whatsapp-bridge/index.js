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

  const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
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
    })
  });

  if (!response.ok) {
    const body = await response.text();
    throw new Error("Groq HTTP " + response.status + ": " + body.slice(0, 180));
  }

  const data = await response.json();
  const text = data.choices && data.choices[0] && data.choices[0].message && data.choices[0].message.content;
  if (!text) throw new Error("Groq mengembalikan respon kosong");
  return JSON.parse(text);
}

async function parseTaskDraft(originalText, deadlineAnswer) {
  const context = deadlineAnswer
    ? 'Command awal: "' + originalText + '"\nJawaban deadline: "' + deadlineAnswer + '"'
    : 'Command: "' + originalText + '"';

  const parsed = await groqJson([
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

async function sendToSelf(text) {
  if (state.selfChatJid) await client.sendMessage(state.selfChatJid, text);
}

client.on("message_create", async (msg) => {
  try {
    const text = String(msg.body || "").trim();
    if (!text || !msg.fromMe || !whatsappReady) return;

    const ownPhoneJid = client.info && client.info.wid && client.info.wid._serialized;

    // Primary-phone messages come from the phone JID.
    // Messages generated by this Web session use the self LID, so ignore them.
    if (ownPhoneJid && msg.from !== ownPhoneJid) return;

    if (text.startsWith("+")) {
      if (!state.selfChatJid) {
        state.selfChatJid = msg.to;
        saveState();
        console.log("Self-chat dikunci ke:", state.selfChatJid);
      }

      if (msg.to !== state.selfChatJid) {
        console.log("Command + diabaikan karena bukan chat diri sendiri.");
        return;
      }

      const command = text.slice(1).trim();
      if (!command) {
        await sendToSelf("Tulis tugas setelah +. Contoh: + ada PR B Indo");
        return;
      }

      const draft = await parseTaskDraft(command);
      state.conversation = {
        step: draft.needsDeadline ? "deadline" : "reminder",
        draft
      };
      saveState();

      if (draft.needsDeadline) {
        await sendToSelf('Oke, "' + draft.title + '". Deadline-nya kapan?');
      } else {
        await sendToSelf(
          "Kebaca: " + draft.title +
          "\nDeadline: " + formatDeadline(draft.deadlineEpochMillis) +
          "\nMau diingatkan kapan? Contoh: 30 menit sebelumnya, 1 jam sebelumnya, pas deadline, atau gausah."
        );
      }
      return;
    }

    if (!state.selfChatJid || msg.to !== state.selfChatJid || !state.conversation) return;

    if (state.conversation.step === "deadline") {
      const updated = await parseTaskDraft(state.conversation.draft.originalText, text);

      if (updated.needsDeadline || !updated.deadlineEpochMillis) {
        await sendToSelf("Aku belum nangkep waktunya. Contoh: besok jam 8 pagi atau Jumat jam 16.00.");
        return;
      }

      state.conversation = { step: "reminder", draft: updated };
      saveState();

      await sendToSelf(
        "Sip. Deadline " + formatDeadline(updated.deadlineEpochMillis) +
        ".\nMau diingatkan kapan? Contoh: 30 menit sebelumnya, 1 jam sebelumnya, pas deadline, atau gausah."
      );
      return;
    }

    if (state.conversation.step === "reminder") {
      const reminderMinutesBefore = parseReminder(text);

      if (reminderMinutesBefore === null) {
        await sendToSelf("Pilih misalnya: 30 menit sebelumnya, 1 jam sebelumnya, pas deadline, atau gausah.");
        return;
      }

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

      await sendToSelf(
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
      await sendToSelf("Ada error saat membaca tugas. Coba lagi dengan command + baru.");
    } catch {}
  }
});

client.initialize().catch((error) => {
  console.error("WhatsApp init error:", error);
});
