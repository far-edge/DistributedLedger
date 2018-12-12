FROM node:8

# Create app directory
RUN mkdir -p /usr/app/src
COPY ./app /usr/app/src
WORKDIR /usr/app/

# Install app dependencies
COPY ./app/package.json /usr/app/

RUN npm install --production

# Copy app source
#COPY src /usr/src/app/

# Remove dev dependencies
RUN npm prune --production

# Expose port and CMD
EXPOSE 3026
CMD [ "npm", "start" ]