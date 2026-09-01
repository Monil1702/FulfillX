import amqp from "amqplib";
import type { Server } from "socket.io";
import type { Logger } from "pino";
import { toNotification, type Notification } from "./notification.js";

const exchange = "fulfillx.notifications";
const queue = "fulfillx.notification.commands";

export async function consumeNotifications(
  url: string,
  io: Server,
  history: Notification[],
  log: Logger
): Promise<void> {
  const connection = await amqp.connect(url);
  const channel = await connection.createChannel();
  await channel.assertExchange(exchange, "topic", { durable: true });
  await channel.assertQueue(queue, { durable: true });
  await channel.bindQueue(queue, exchange, "notification.order.*");
  await channel.prefetch(10);

  connection.on("error", (error) => log.error({ error }, "RabbitMQ connection error"));
  connection.on("close", () => {
    log.warn("RabbitMQ connection closed; reconnecting");
    setTimeout(() => void connectWithRetry(url, io, history, log), 3000);
  });

  await channel.consume(queue, (message) => {
    if (!message) return;
    try {
      const notification = toNotification(JSON.parse(message.content.toString("utf8")));
      history.unshift(notification);
      history.splice(100);
      io.emit("notification", notification);
      channel.ack(message);
      log.info({ notificationId: notification.id, orderId: notification.orderId }, "Notification delivered");
    } catch (error) {
      log.error({ error }, "Invalid notification command moved out of the queue");
      channel.nack(message, false, false);
    }
  });
  log.info({ exchange, queue }, "RabbitMQ consumer ready");
}

export async function connectWithRetry(
  url: string,
  io: Server,
  history: Notification[],
  log: Logger
): Promise<void> {
  try {
    await consumeNotifications(url, io, history, log);
  } catch (error) {
    log.warn({ error }, "RabbitMQ unavailable; retrying in 3 seconds");
    setTimeout(() => void connectWithRetry(url, io, history, log), 3000);
  }
}

