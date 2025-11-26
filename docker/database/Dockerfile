FROM postgres:18

RUN apt-get update \
    && apt-get install -y locales \
    && sed -i 's/^# *\(nb_NO.UTF-8\)/\1/' /etc/locale.gen \
    && locale-gen nb_NO.UTF-8 \
    && update-locale

ENV LANG="nb_NO.UTF-8"
ENV LC_COLLATE="nb_NO.UTF-8"
ENV LC_ALL="nb_NO.UTF-8"
