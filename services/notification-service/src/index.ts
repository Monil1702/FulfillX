import { createServer } from "node:http";
import cors from "cors";
import express from "express";
import helmet from "helmet";
import pino from "pino";
import { pinoHttp } from "pino-http";
import { Server } from "socket.io";
import { connectWithRetry } from "./broker.js";
import type { Notification } from "./notification.js";

const port = Number(process.env.PORT ?? 3001);
const rabbitUrl = process.env.RABBITMQ_URL;
if (!rabbitUrl) {
  throw new Error("RABBITMQ_URL must be configured");
}
const origins = (process.env.ALLOWED_ORIGINS ?? "http://localhost:3000,http://localhost:5173").split(",");
const log = pino({ level: process.env.LOG_LEVEL ?? "info" });
const app = express();
const server = createServer(app);
const io = new Server(server, { cors: { origin: origins } });
const history: Notification[] = [];

app.use(helmet());
app.use(cors({ origin: origins }));
app.use(express.json());
app.use(pinoHttp({ logger: log, genReqId: (req) => req.headers["x-correlation-id"]?.toString() ?? crypto.randomUUID() }));

app.get("/health", (_request, response) => response.json({ status: "UP", service: "notification-service" }));
app.get("/api/notifications", (_request, response) => response.json(history));

io.on("connection", (socket) => {
  log.info({ socketId: socket.id }, "Dashboard connected");
});

server.listen(port, () => {
  log.info({ port }, "Notification service listening");
  void connectWithRetry(rabbitUrl, io, history, log);
});
