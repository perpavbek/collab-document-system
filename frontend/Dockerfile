FROM node:20 AS build
WORKDIR /app

COPY package*.json ./
RUN npm install

COPY . .

ARG API_URL
ARG WS_URL

ENV VITE_API_BASE_URL=$API_URL
ENV VITE_WS_BASE_URL=$WS_URL

RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80