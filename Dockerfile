FROM openjdk:8-jdk-alpine
CMD ["gradle"]

# copied from https://github.com/keeganwitt/docker-gradle/blob/cfa58146fc5760ea964dbd5f816111e9430ce304/jdk8-alpine/Dockerfile
# but removed the VOLUME so openshift builds work

ENV GRADLE_HOME /opt/gradle
ENV GRADLE_VERSION 4.5.1

ARG GRADLE_DOWNLOAD_SHA256=3e2ea0d8b96605b7c528768f646e0975bd9822f06df1f04a64fd279b1a17805e
RUN set -o errexit -o nounset \
	&& echo "Installing build dependencies" \
	&& apk add --no-cache --virtual .build-deps \
		ca-certificates \
		openssl \
		unzip \
	\
	&& echo "Downloading Gradle" \
	&& wget -O gradle.zip "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" \
	\
	&& echo "Checking download hash" \
	&& echo "${GRADLE_DOWNLOAD_SHA256} *gradle.zip" | sha256sum -c - \
	\
	&& echo "Installing Gradle" \
	&& unzip gradle.zip \
	&& rm gradle.zip \
	&& mkdir /opt \
	&& mv "gradle-${GRADLE_VERSION}" "${GRADLE_HOME}/" \
	&& ln -s "${GRADLE_HOME}/bin/gradle" /usr/bin/gradle \
	\
	&& apk del .build-deps \
	\
	&& echo "Adding gradle user and group" \
	&& addgroup -S -g 1000 gradle \
	&& adduser -D -S -G gradle -u 1000 -s /bin/ash gradle \
	&& mkdir /home/gradle/.gradle \
	&& chown -R gradle:gradle /home/gradle \
	\
	&& echo "Symlinking root Gradle cache to gradle Gradle cache" \
	&& ln -s /home/gradle/.gradle /root/.gradle

USER gradle
WORKDIR /home/gradle

RUN set -o errexit -o nounset \
	&& echo "Testing Gradle installation" \
	&& gradle --version

RUN whoami ; pwd ; gradle --version
ADD build.gradle .
COPY src src
RUN find src
RUN gradle build --info && find . -type f -name 'hits-*.jar'
