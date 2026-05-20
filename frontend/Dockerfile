FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY angular.json postcss.config.js tailwind.config.js tsconfig*.json ./
COPY src src
EXPOSE 4200
CMD ["npm", "start"]
