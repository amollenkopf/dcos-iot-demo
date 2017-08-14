#!/bin/sh
if [ "$#" -ne 3 ];then
  PLAYAPP_HOME=..
  HTTP_PORT=9000
  HTTPS_PORT=9443
else
  PLAYAPP_HOME=$1
  HTTP_PORT=$2
  HTTPS_PORT=$3
fi

PASSWORD=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1)
JKS_FILE="$PLAYAPP_HOME/conf/proxy.jks"
SERVER_CERT_ALIAS="Proxy-Certificate"

if [[ -n "${PRIVATE_KEY+set}" ]] && [[ -n "${HTTPS_CERT+set}" ]]; then
  echo 'We have what we need to configure https'
  PRIVATE_KEY_PEM="$PLAYAPP_HOME/conf/proxy-privkey.pem"
  HTTPS_CERT_PEM="$PLAYAPP_HOME/conf/proxy-cert.pem"
  HTTPS_CERT_LIST_PEM="$PLAYAPP_HOME/conf/proxy-ca-chain-certs.pem"
  P12_FILE="$PLAYAPP_HOME/conf/proxy.p12"
  CHAIN_DIR="$PLAYAPP_HOME/conf/chain"
  echo "$PRIVATE_KEY" > "$PRIVATE_KEY_PEM"
  echo "$HTTPS_CERT" > "$HTTPS_CERT_PEM"
  openssl pkcs12 -export -in "$HTTPS_CERT_PEM" -inkey "$PRIVATE_KEY_PEM" -name "$SERVER_CERT_ALIAS" -out "$P12_FILE" -password pass:$PASSWORD
  keytool -genkey -keyalg RSA -alias deleteme -keystore $JKS_FILE -storepass $PASSWORD -keypass $PASSWORD -dname "CN=deleteme"
  keytool -delete -alias deleteme -keystore $JKS_FILE -storepass $PASSWORD
  if [[ -n "${HTTPS_CERT_CHAIN+set}" ]]; then
    mkdir -p $CHAIN_DIR
    echo "$HTTPS_CERT_CHAIN" | awk -v awk_dir=$CHAIN_DIR/cert 'split_after==1{n++;split_after=0} /-----END CERTIFICATE-----/ {split_after=1} {print > (awk_dir n ".pem")}' 
    COUNT=1
    for file in $CHAIN_DIR/*.pem;
    do 
      echo "$file"
      keytool -import -v -noprompt -trustcacerts -alias chain$COUNT -file $file -keystore $JKS_FILE -storepass $PASSWORD
      COUNT=`expr $COUNT + 1`
    done
  fi

  keytool -v -importkeystore -srckeystore $P12_FILE -srcstoretype pkcs12 -srcstorepass $PASSWORD -destkeystore $JKS_FILE -deststoretype jks -deststorepass $PASSWORD
  rm -rf "$CHAIN_DIR" "$P12_FILE"
else
  echo 'Will generate a self-signed certifcate....'
  keytool -genkey -alias $SERVER_CERT_ALIAS -keyalg RSA -keysize 2048 -dname "CN=localhost,O=SelfSignedCertificate" -keystore $JKS_FILE -storepass $PASSWORD -keypass $PASSWORD -validity 7300
fi
unset PRIVATE_KEY

$PLAYAPP_HOME/bin/webapp -Dhttps.port=$HTTPS_PORT -Dhttp.port=$HTTP_PORT -Dplay.crypto.secret=$PASSWORD -Dplay.server.https.keyStore.path="$JKS_FILE" -Dplay.server.https.keyStore.password=$PASSWORD -Dplay.server.https.keyStore.type=jks
