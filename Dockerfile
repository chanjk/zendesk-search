FROM adoptopenjdk/openjdk11:alpine-slim

ARG SBT_VERSION=1.4.6

RUN apk --no-cache add bash ncurses

RUN apk --no-cache --virtual .build-deps add curl \
    && curl -sSL https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz | tar -xvz -C /usr/local \
    && ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt \
    && apk del .build-deps
