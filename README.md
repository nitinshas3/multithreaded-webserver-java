# Multithreaded Web Server in Java

A TCP server built from scratch on raw sockets — no Spring, no Netty, no `HttpServer`. Two implementations side by side: a **single-threaded** version that serves one client at a time, and a **multithreaded** version that handles concurrent clients. The point of the repo is to make the difference measurable.

**Stack:** Java (`java.net`, `java.io`, `java.util.function`) · Zero dependencies

---

## The Problem

A single-threaded server blocks on `accept()` → serve → `close()`. Client #2 waits for client #1 to finish. With *n* clients and *t* ms of work each, the last client waits **O(n·t)**.

The multithreaded version hands each accepted socket to its own thread, so the accept loop returns immediately. Wall-clock time drops to roughly **O(t)** while CPU and memory carry the cost instead.

```
                  ┌──────────────┐
   Client ────────►  ServerSocket │  accept()  ── blocks until a client connects
                  └──────┬───────┘
                         │ returns a Socket (the actual data pipe)
        ┌────────────────┴────────────────┐
        │                                 │
  Single-threaded                   Multithreaded
  handle inline,                    new Thread(() -> consumer.accept(socket))
  next client waits                 accept loop frees up instantly
```

---

## Run It

```bash
# from the repo root
javac SingleThreadedServer/*.java MultiThreadedServer/*.java

# terminal 1 — start a server
java SingleThreadedServer.Server
java MultiThreadedServer.Server      # ...or this one

# terminal 2 — fire clients at it
java SingleThreadedServer.Client     # 1 client
java MultiThreadedServer.Client      # 100 concurrent clients
```

Default port: **8010**. Run the multithreaded client against the single-threaded server to watch the queue back up.

---

## Repo Layout

| Path | What's in it |
|---|---|
| `SingleThreadedServer/` | Baseline. `Server.java` handles one connection at a time; `Client.java` opens one socket. |
| `MultiThreadedServer/` | `Server.java` spawns a thread per connection using a `Consumer<Socket>` lambda; `Client.java` launches 100 concurrent clients to load it. |

---

## What I Learned

<details>
<summary><b>How data actually travels</b> — ISP, IP, ports, DNS, MAC, ARP</summary>

<br>

| Concept | The one-line version |
|---|---|
| **ISP** | Your gateway to the wider internet; owns the routing path from your router to the backbone. |
| **IP address** | Identifies a *machine* on a network. Logical, assignable, changes when you change networks. |
| **Port** | Identifies a *process* on that machine. 80 = HTTP, 5432 = Postgres, 8010 = this server. |
| **Socket** | `IP + port` — the full address of one endpoint. A connection is a pair of them. |
| **DNS** | Name → IP lookup. `InetAddress.getByName("localhost")` in the client is a DNS resolution. |
| **MAC address** | Identifies a *NIC* on the local link. Burned in, not routable beyond the LAN. |
| **ARP** | Resolves "which MAC owns this local IP?" — the glue between the IP layer and the link layer. |
| **IPv4 vs IPv6** | 32-bit (~4.3B addresses, exhausted, hence NAT) vs 128-bit (effectively unlimited, no NAT needed). |

The chain for one request: DNS resolves the name → IP routes the packet across networks → ARP finds the MAC for the final hop → the port picks the process → the socket delivers the bytes.

</details>

<details>
<summary><b>TCP</b> — handshake, connection state, termination</summary>

<br>

**Opening (3-way handshake)** — `new Socket(address, port)` triggers this:

```
Client ──SYN──────────────► Server     "let's talk, my seq is x"
Client ◄─────────SYN-ACK─── Server     "ok, my seq is y, saw your x"
Client ──ACK──────────────► Server     "saw your y"   → connection ESTABLISHED
```

**Closing (4-way)** — `socket.close()` triggers this. Each direction shuts down independently, because TCP is full-duplex: `FIN → ACK`, then `FIN → ACK` the other way. The closing side sits in `TIME_WAIT` (~2× max segment lifetime) so stragglers don't leak into the next connection on the same port pair.

This is why an unclosed socket matters — the OS holds the file descriptor and the port pair, and you eventually run out of both.

</details>

<details>
<summary><b>The Socket API in Java</b> — ServerSocket vs Socket, streams</summary>

<br>

- **`ServerSocket`** only *listens*. It binds a port and blocks on `accept()`. It never carries data.
- **`accept()`** returns a **`Socket`** — that's the actual bidirectional pipe for this one client.
- **`setSoTimeout(ms)`** makes `accept()` throw `SocketTimeoutException` instead of blocking forever.

Streams are the read/write layer on top of the socket:

```
socket.getInputStream()   →  raw bytes
  └─ InputStreamReader    →  bytes decoded into characters
      └─ BufferedReader   →  readLine(), buffered so you aren't syscalling per byte

socket.getOutputStream()  →  raw bytes
  └─ PrintWriter          →  encodes + formats + (optionally) auto-flushes
```

`PrintWriter` buffers by default — without `flush()` (or `new PrintWriter(out, true)` for autoflush), your response sits in memory and the client blocks on `readLine()` forever.

</details>

<details>
<summary><b>Concurrency</b> — Thread, Runnable, functional interfaces, lambdas</summary>

<br>

- **`Thread`** is the worker; **`Runnable`** is the job. Separating them means the same job can run on any thread, or on a pool.
- A **functional interface** has exactly one abstract method, so it can be written as a **lambda**. `Runnable` (`run()`), `Consumer<T>` (`accept(T)`), `Supplier<T>`, `Function<T,R>`.
- The multithreaded server stores its per-client logic as a `Consumer<Socket>` lambda and hands it to a fresh thread — the handler becomes a value you pass around, not a class you subclass.

```java
Consumer<Socket> handler = clientSocket -> { /* write response, close */ };
new Thread(() -> handler.accept(acceptedConnection)).start();
```

The anonymous-inner-class form (`new Runnable() { public void run() {...} }`) in the client is the pre-Java-8 equivalent — same bytecode intent, more ceremony.

</details>

---

## Known Limits & Roadmap

Honest about where this stands:

- [ ] **Thread pooling.** Thread-per-connection is unbounded — 10k clients means 10k threads (~1MB stack each) and the scheduler drowns in context switches. Next step: `ExecutorService` with a fixed pool, so threads are reused and concurrency is capped.
- [ ] **Remove `setSoTimeout(10000)`** from the multithreaded server — it kills the accept loop after 10s of idle.
- [ ] **Load testing** with JMeter to put real numbers on the single vs multi comparison.
- [ ] **Actual HTTP parsing.** Right now it speaks a fixed greeting, not HTTP — request-line and header parsing is the next layer up.
- [ ] **Non-blocking I/O** (NIO / `Selector`) as the endgame: one thread, many connections, no per-connection cost.

---

*Built to understand how servers work underneath the frameworks.*

---

**Nitin S Shastri** · [@nitinshas3](https://github.com/nitinshas3)