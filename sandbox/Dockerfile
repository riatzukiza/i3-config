# ubuntu:24.04 ships i3 4.23, matching the host exactly — version fidelity
# matters: the sandbox exists to reproduce host config behavior.
FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive \
    DISPLAY=:99 \
    XDG_RUNTIME_DIR=/tmp/xdg-runtime \
    I3SOCK=/tmp/i3-ipc.sock

RUN apt-get update && apt-get install -y --no-install-recommends \
    i3-wm xvfb xdotool jq rofi xterm \
    x11-apps x11-utils xauth dbus-x11 \
    imagemagick bash ca-certificates curl \
    && rm -rf /var/lib/apt/lists/*

RUN userdel -r ubuntu 2>/dev/null || true \
    && useradd -m -u 1000 -s /bin/bash agent \
    && mkdir -p /tmp/.X11-unix /tmp/xdg-runtime \
    && chmod 1777 /tmp/.X11-unix \
    && chmod 700 /tmp/xdg-runtime \
    && chown agent:agent /tmp/xdg-runtime /tmp/.X11-unix

COPY scripts/ /home/agent/bin/
RUN chmod +x /home/agent/bin/* && chown -R agent:agent /home/agent/bin

USER agent
WORKDIR /workspace

ENV PATH="/home/agent/bin:${PATH}"

ENTRYPOINT ["/home/agent/bin/entrypoint"]
CMD ["sleep", "infinity"]
