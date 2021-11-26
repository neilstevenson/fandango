FROM library/node:16.13.0

COPY target/classes/package.json ./
COPY target/classes/client.js ./

RUN npm install -g npm@8.1.0
RUN npm install hazelcast-client
CMD [ "npm", "--loglevel=error", "start" ]
