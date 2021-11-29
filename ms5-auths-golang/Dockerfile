FROM library/golang:1.17.3-buster

WORKDIR /root

COPY target/classes/go.mod /root/
COPY target/classes/go.sum /root/
COPY target/classes/fandango/   /root/fandango/

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo ./fandango/client

CMD ["./client"]
