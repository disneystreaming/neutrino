FROM azul/zulu-openjdk:8
WORKDIR /var/jenkins_home

RUN apt-get update && \
	apt-get install -y curl wget && \
	apt-get install -y sudo

# There's a default '-u 666:666' added in jenkins docker command
# scoverage and spark testing base works in docker ONLY when the user/group exists
RUN groupadd -g 666 jenkins && useradd -u 666 -g jenkins --shell /bin/bash --home-dir /var/jenkins_home jenkins
RUN chown 666:666 /var/jenkins_home
RUN chmod -R 777 /var/jenkins_home
RUN echo 'jenkins ALL=NOPASSWD: ALL' >> /etc/sudoers.d/50-jenkins
RUN echo 'Defaults    env_keep += "DEBIAN_FRONTEND"' >> /etc/sudoers.d/env_keep
USER jenkins