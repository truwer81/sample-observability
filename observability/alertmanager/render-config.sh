#!/bin/sh
set -eu

TEMPLATE_FILE="/etc/alertmanager/alertmanager.yml"
GENERATED_FILE="/tmp/alertmanager.yml"
SMTP_PASSWORD_FILE="/run/secrets/smtp_password"

require_env() {
  var_name="$1"
  eval "value=\${$var_name:-}"
  if [ -z "$value" ]; then
    echo "Missing required environment variable: $var_name" >&2
    exit 1
  fi
}

escape_for_template() {
  printf '%s' "$1" | sed \
    -e 's/\\/\\\\/g' \
    -e 's/"/\\"/g' \
    -e 's/[&|]/\\&/g'
}

render_var() {
  var_name="$1"
  eval "raw_value=\${$var_name}"
  escaped_value="$(escape_for_template "$raw_value")"
  sed -i "s|\${$var_name}|$escaped_value|g" "$GENERATED_FILE"
}

require_env SMTP_SMARTHOST
require_env SMTP_FROM
require_env SMTP_USERNAME
require_env SMTP_TO

if [ ! -s "$SMTP_PASSWORD_FILE" ]; then
  echo "SMTP password secret file is missing or empty: $SMTP_PASSWORD_FILE" >&2
  exit 1
fi

cp "$TEMPLATE_FILE" "$GENERATED_FILE"
render_var SMTP_SMARTHOST
render_var SMTP_FROM
render_var SMTP_USERNAME
render_var SMTP_TO

exec /bin/alertmanager "$@"
