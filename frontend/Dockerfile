FROM node:24-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG VITE_ORDER_API_URL=http://localhost:8080
ARG VITE_INVENTORY_API_URL=http://localhost:8081
ARG VITE_NOTIFICATION_API_URL=http://localhost:3001
ENV VITE_ORDER_API_URL=$VITE_ORDER_API_URL
ENV VITE_INVENTORY_API_URL=$VITE_INVENTORY_API_URL
ENV VITE_NOTIFICATION_API_URL=$VITE_NOTIFICATION_API_URL
RUN npm run build

FROM nginx:1.29-alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80

