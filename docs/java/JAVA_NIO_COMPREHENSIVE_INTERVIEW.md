# Java NIO (New I/O) Complete Guide

## Table of Contents
- [Introduction](#introduction)
- [Core Components](#core-components)
- [Buffers](#buffers)
- [Channels](#channels)
- [Selectors](#selectors)
- [Files and Paths](#files-and-paths)
- [Asynchronous I/O](#asynchronous-io)
- [Memory-Mapped Files](#memory-mapped-files)
- [NIO vs Traditional I/O](#nio-vs-traditional-io)
- [Best Practices](#best-practices)

## Introduction

Java NIO (New I/O) is an alternative I/O API introduced in Java 1.4, providing:
- **Non-blocking I/O**: Operations don't wait for data
- **Buffer-oriented**: Data is read/written from/to buffers
- **Channel-based**: Uses channels for data transfer
- **Selectors**: Single thread manages multiple channels

## Core Components

### Key Concepts
1. **Channels**: Connections to I/O entities
2. **Buffers**: Containers for data
3. **Selectors**: Multiplexed non-blocking I/O
4. **Charsets**: Character encoding/decoding

## Buffers

### Buffer Basics
```java
// Creating buffers
ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
CharBuffer charBuffer = CharBuffer.allocate(256);
IntBuffer intBuffer = IntBuffer.allocate(128);

// Direct buffers (allocated outside heap)
ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

// Wrapping existing arrays
byte[] bytes = new byte[1024];
ByteBuffer wrapped = ByteBuffer.wrap(bytes);
```

### Buffer Properties
```java
public class BufferExample {
    public static void demonstrateBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(10);

        // Properties
        System.out.println("Capacity: " + buffer.capacity()); // 10
        System.out.println("Position: " + buffer.position()); // 0
        System.out.println("Limit: " + buffer.limit());       // 10

        // Writing data
        buffer.put((byte) 1);
        buffer.put((byte) 2);
        buffer.put((byte) 3);

        // Flip for reading
        buffer.flip();
        System.out.println("After flip - Position: " + buffer.position()); // 0
        System.out.println("After flip - Limit: " + buffer.limit());       // 3

        // Reading data
        while (buffer.hasRemaining()) {
            System.out.println(buffer.get());
        }

        // Clear for reuse
        buffer.clear();

        // Compact (move remaining data to beginning)
        buffer.put((byte) 4);
        buffer.put((byte) 5);
        buffer.flip();
        buffer.get(); // Read one
        buffer.compact(); // Move remaining to start
    }
}
```

### Buffer Operations
```java
public class BufferOperations {
    // Mark and reset
    public void markAndReset() {
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.position(2);
        buffer.mark();      // Mark current position
        buffer.position(5);
        buffer.reset();     // Return to marked position
    }

    // Bulk operations
    public void bulkOperations() {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        byte[] data = new byte[50];

        // Bulk put
        buffer.put(data);

        // Bulk get
        buffer.flip();
        byte[] destination = new byte[25];
        buffer.get(destination);
    }

    // View buffers
    public void viewBuffers() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(32);

        // Different views of same data
        IntBuffer intView = byteBuffer.asIntBuffer();
        FloatBuffer floatView = byteBuffer.asFloatBuffer();
        CharBuffer charView = byteBuffer.asCharBuffer();

        // Changes in one view affect others
        intView.put(42);
        System.out.println(byteBuffer.getInt(0)); // 42
    }
}
```

## Channels

### FileChannel
```java
public class FileChannelExample {
    // Reading from file
    public void readFile(String filename) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (channel.read(buffer) != -1) {
                buffer.flip();
                while (buffer.hasRemaining()) {
                    System.out.print((char) buffer.get());
                }
                buffer.clear();
            }
        }
    }

    // Writing to file
    public void writeFile(String filename, String data) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw");
             FileChannel channel = file.getChannel()) {

            ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());
            channel.write(buffer);
        }
    }

    // Transferring between channels
    public void transferChannels(String from, String to) throws IOException {
        try (RandomAccessFile sourceFile = new RandomAccessFile(from, "r");
             RandomAccessFile destFile = new RandomAccessFile(to, "rw");
             FileChannel sourceChannel = sourceFile.getChannel();
             FileChannel destChannel = destFile.getChannel()) {

            // Direct transfer
            long position = 0;
            long count = sourceChannel.size();
            destChannel.transferFrom(sourceChannel, position, count);

            // Or use transferTo
            // sourceChannel.transferTo(position, count, destChannel);
        }
    }

    // File locking
    public void lockFile(String filename) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filename, "rw");
             FileChannel channel = file.getChannel()) {

            // Exclusive lock on entire file
            FileLock lock = channel.lock();
            try {
                // Perform operations
                System.out.println("File locked");
            } finally {
                lock.release();
            }

            // Shared lock on portion
            FileLock sharedLock = channel.lock(0, 100, true);
            sharedLock.release();

            // Try lock (non-blocking)
            FileLock tryLock = channel.tryLock();
            if (tryLock != null) {
                tryLock.release();
            }
        }
    }
}
```

### SocketChannel & ServerSocketChannel
```java
public class SocketChannelExample {
    // Client
    public void client() throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("localhost", 8080));

        String message = "Hello Server";
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        socketChannel.write(buffer);

        buffer.clear();
        socketChannel.read(buffer);
        buffer.flip();

        socketChannel.close();
    }

    // Server
    public void server() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(8080));

        while (true) {
            SocketChannel clientChannel = serverChannel.accept();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            clientChannel.read(buffer);

            buffer.flip();
            clientChannel.write(buffer);

            clientChannel.close();
        }
    }

    // Non-blocking mode
    public void nonBlockingServer() throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8080));

        while (true) {
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                clientChannel.configureBlocking(false);
                // Handle client
            }
            // Can do other work here
        }
    }
}
```

### DatagramChannel
```java
public class DatagramChannelExample {
    // UDP Server
    public void udpServer() throws IOException {
        DatagramChannel channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(9999));

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        while (true) {
            buffer.clear();
            SocketAddress clientAddr = channel.receive(buffer);

            if (clientAddr != null) {
                buffer.flip();
                channel.send(buffer, clientAddr);
            }
        }
    }

    // UDP Client
    public void udpClient() throws IOException {
        DatagramChannel channel = DatagramChannel.open();

        String message = "Hello UDP";
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());

        InetSocketAddress serverAddr = new InetSocketAddress("localhost", 9999);
        channel.send(buffer, serverAddr);

        buffer.clear();
        channel.receive(buffer);

        channel.close();
    }
}
```

### Pipe
```java
public class PipeExample {
    public void usePipe() throws IOException {
        // Create pipe
        Pipe pipe = Pipe.open();

        // Write to pipe
        Pipe.SinkChannel sinkChannel = pipe.sink();
        ByteBuffer buffer = ByteBuffer.allocate(48);
        buffer.put("Hello Pipe".getBytes());
        buffer.flip();

        while (buffer.hasRemaining()) {
            sinkChannel.write(buffer);
        }

        // Read from pipe
        Pipe.SourceChannel sourceChannel = pipe.source();
        ByteBuffer readBuffer = ByteBuffer.allocate(48);
        sourceChannel.read(readBuffer);

        readBuffer.flip();
        while (readBuffer.hasRemaining()) {
            System.out.print((char) readBuffer.get());
        }
    }
}
```

## Selectors

### Selector Basics
```java
public class SelectorExample {
    public void selectorServer() throws IOException {
        // Create selector
        Selector selector = Selector.open();

        // Create server channel
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8080));

        // Register channel with selector
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            // Block until events
            int readyChannels = selector.select();

            if (readyChannels == 0) continue;

            // Process selected keys
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();

                if (key.isAcceptable()) {
                    handleAccept(key, selector);
                } else if (key.isReadable()) {
                    handleRead(key);
                } else if (key.isWritable()) {
                    handleWrite(key);
                }

                keyIterator.remove();
            }
        }
    }

    private void handleAccept(SelectionKey key, Selector selector)
            throws IOException {
        ServerSocketChannel serverChannel =
            (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            channel.close();
            return;
        }

        buffer.flip();
        // Process data

        // Switch to write mode
        key.interestOps(SelectionKey.OP_WRITE);
        key.attach(buffer);
    }

    private void handleWrite(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        channel.write(buffer);

        if (!buffer.hasRemaining()) {
            // Switch back to read mode
            key.interestOps(SelectionKey.OP_READ);
            key.attach(null);
        }
    }
}
```

### Advanced Selector Usage
```java
public class AdvancedSelector {
    private Selector selector;
    private ByteBuffer buffer = ByteBuffer.allocate(1024);

    public void init() throws IOException {
        selector = Selector.open();
    }

    // Non-blocking timeout
    public void selectWithTimeout() throws IOException {
        // Wait up to 5 seconds
        int readyChannels = selector.select(5000);

        if (readyChannels == 0) {
            System.out.println("Timeout - no channels ready");
        }
    }

    // Immediate return
    public void selectNow() throws IOException {
        int readyChannels = selector.selectNow();
        // Returns immediately, doesn't block
    }

    // Wake up selector
    public void wakeupSelector() {
        // From another thread
        selector.wakeup();
    }

    // Attach data to keys
    class Attachment {
        ByteBuffer readBuffer;
        ByteBuffer writeBuffer;
        boolean isReading;
    }

    public void attachData(SelectionKey key) {
        Attachment attachment = new Attachment();
        attachment.readBuffer = ByteBuffer.allocate(1024);
        attachment.writeBuffer = ByteBuffer.allocate(1024);
        key.attach(attachment);
    }
}
```

## Files and Paths

### Path Operations
```java
public class PathOperations {
    public void pathBasics() {
        // Creating paths
        Path path1 = Paths.get("/home/user/file.txt");
        Path path2 = Paths.get("/home", "user", "file.txt");
        Path path3 = FileSystems.getDefault().getPath("/home/user");

        // Path information
        System.out.println("File name: " + path1.getFileName());
        System.out.println("Parent: " + path1.getParent());
        System.out.println("Root: " + path1.getRoot());
        System.out.println("Name count: " + path1.getNameCount());

        // Path components
        for (int i = 0; i < path1.getNameCount(); i++) {
            System.out.println("Name " + i + ": " + path1.getName(i));
        }

        // Path operations
        Path absolute = path1.toAbsolutePath();
        Path normalized = Paths.get("/home/./user/../user/file.txt").normalize();
        Path resolved = path1.resolve("subdir/file2.txt");
        Path relativized = path1.relativize(path2);

        // Path comparison
        boolean isSame = path1.equals(path2);
        boolean startsWith = path1.startsWith("/home");
        boolean endsWith = path1.endsWith("file.txt");
    }
}
```

### Files Utility Class
```java
public class FilesUtility {
    public void fileOperations() throws IOException {
        Path path = Paths.get("example.txt");

        // Check existence
        boolean exists = Files.exists(path);
        boolean notExists = Files.notExists(path);

        // File attributes
        boolean isDirectory = Files.isDirectory(path);
        boolean isRegularFile = Files.isRegularFile(path);
        boolean isReadable = Files.isReadable(path);
        boolean isWritable = Files.isWritable(path);
        boolean isExecutable = Files.isExecutable(path);
        boolean isHidden = Files.isHidden(path);

        // File size
        long size = Files.size(path);

        // Timestamps
        FileTime lastModified = Files.getLastModifiedTime(path);
        Files.setLastModifiedTime(path, FileTime.fromMillis(System.currentTimeMillis()));

        // Owner
        UserPrincipal owner = Files.getOwner(path);

        // Permissions (POSIX)
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);
    }

    public void fileCreationAndDeletion() throws IOException {
        // Create file
        Path newFile = Paths.get("new-file.txt");
        Files.createFile(newFile);

        // Create directories
        Path newDir = Paths.get("new-dir");
        Files.createDirectory(newDir);
        Files.createDirectories(Paths.get("parent/child/grandchild"));

        // Create temp file/directory
        Path tempFile = Files.createTempFile("prefix", ".suffix");
        Path tempDir = Files.createTempDirectory("prefix");

        // Delete
        Files.delete(newFile);
        Files.deleteIfExists(newFile);
    }

    public void copyAndMove() throws IOException {
        Path source = Paths.get("source.txt");
        Path target = Paths.get("target.txt");

        // Copy
        Files.copy(source, target);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        Files.copy(source, target,
            StandardCopyOption.COPY_ATTRIBUTES,
            StandardCopyOption.REPLACE_EXISTING);

        // Move
        Files.move(source, target);
        Files.move(source, target, StandardCopyOption.ATOMIC_MOVE);
    }

    public void readingAndWriting() throws IOException {
        Path path = Paths.get("file.txt");

        // Read all bytes
        byte[] bytes = Files.readAllBytes(path);

        // Read all lines
        List<String> lines = Files.readAllLines(path);
        List<String> linesWithCharset = Files.readAllLines(path, StandardCharsets.UTF_8);

        // Write bytes
        Files.write(path, bytes);

        // Write lines
        List<String> linesToWrite = Arrays.asList("Line 1", "Line 2");
        Files.write(path, linesToWrite);
        Files.write(path, linesToWrite, StandardOpenOption.APPEND);

        // Buffered I/O
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("Hello NIO");
            writer.newLine();
        }

        // Stream lines
        try (Stream<String> stream = Files.lines(path)) {
            stream.filter(line -> line.contains("search"))
                  .forEach(System.out::println);
        }
    }
}
```

### Directory Operations
```java
public class DirectoryOperations {
    public void listDirectory() throws IOException {
        Path dir = Paths.get("/home/user");

        // List directory contents
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                System.out.println(entry.getFileName());
            }
        }

        // List with glob pattern
        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(dir, "*.{txt,log}")) {
            for (Path entry : stream) {
                System.out.println(entry);
            }
        }

        // List with filter
        DirectoryStream.Filter<Path> filter = entry -> {
            return Files.isDirectory(entry) && !Files.isHidden(entry);
        };

        try (DirectoryStream<Path> stream =
                Files.newDirectoryStream(dir, filter)) {
            for (Path entry : stream) {
                System.out.println(entry);
            }
        }
    }

    public void walkFileTree() throws IOException {
        Path start = Paths.get("/home/user");

        // Simple file visitor
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
                System.out.println("File: " + file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs) throws IOException {
                System.out.println("Entering: " + dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir,
                    IOException exc) throws IOException {
                System.out.println("Leaving: " + dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file,
                    IOException exc) throws IOException {
                System.err.println("Failed: " + file);
                return FileVisitResult.SKIP_SUBTREE;
            }
        });

        // Walk with max depth
        Files.walkFileTree(start,
            EnumSet.noneOf(FileVisitOption.class),
            2, // max depth
            new SimpleFileVisitor<Path>() {
                // visitor implementation
            });
    }

    public void findFiles() throws IOException {
        Path start = Paths.get("/home/user");

        // Find all .txt files
        PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:*.txt");

        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) {
                if (matcher.matches(file.getFileName())) {
                    System.out.println("Found: " + file);
                }
                return FileVisitResult.CONTINUE;
            }
        });

        // Using Files.walk (Java 8+)
        try (Stream<Path> paths = Files.walk(start)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".txt"))
                 .forEach(System.out::println);
        }

        // Using Files.find (Java 8+)
        try (Stream<Path> paths = Files.find(start,
                Integer.MAX_VALUE,
                (path, attrs) -> attrs.isRegularFile()
                    && path.toString().endsWith(".txt"))) {
            paths.forEach(System.out::println);
        }
    }
}
```

### File Watching
```java
public class FileWatcher {
    public void watchDirectory() throws IOException, InterruptedException {
        Path dir = Paths.get("/home/user/watched");

        try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
            // Register directory
            dir.register(watcher,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY);

            System.out.println("Watching: " + dir);

            while (true) {
                // Wait for events
                WatchKey key = watcher.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    // Context is the file name
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path filename = ev.context();

                    System.out.printf("%s: %s%n", kind.name(), filename);

                    // Resolve against directory
                    Path child = dir.resolve(filename);

                    // Process based on event type
                    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                        System.out.println("Created: " + child);
                    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                        System.out.println("Deleted: " + child);
                    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                        System.out.println("Modified: " + child);
                    }
                }

                // Reset key
                boolean valid = key.reset();
                if (!valid) {
                    break; // Directory no longer accessible
                }
            }
        }
    }

    // Recursive directory watching
    public void watchRecursive(Path start) throws IOException {
        Map<WatchKey, Path> keys = new HashMap<>();
        WatchService watcher = FileSystems.getDefault().newWatchService();

        // Register all directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir,
                    BasicFileAttributes attrs) throws IOException {
                WatchKey key = dir.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);
                keys.put(key, dir);
                return FileVisitResult.CONTINUE;
            }
        });

        // Process events
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                Path child = dir.resolve((Path) event.context());

                // If new directory, register it
                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (Files.isDirectory(child)) {
                        WatchKey newKey = child.register(watcher,
                            StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE,
                            StandardWatchEventKinds.ENTRY_MODIFY);
                        keys.put(newKey, child);
                    }
                }
            }

            if (!key.reset()) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
```

## Asynchronous I/O

### AsynchronousFileChannel
```java
public class AsyncFileChannel {
    public void asyncRead() throws IOException, InterruptedException {
        Path path = Paths.get("async-file.txt");
        AsynchronousFileChannel channel =
            AsynchronousFileChannel.open(path, StandardOpenOption.READ);

        ByteBuffer buffer = ByteBuffer.allocate(1024);

        // Using Future
        Future<Integer> future = channel.read(buffer, 0);

        while (!future.isDone()) {
            System.out.println("Reading...");
            Thread.sleep(100);
        }

        try {
            Integer bytesRead = future.get();
            buffer.flip();
            System.out.println("Bytes read: " + bytesRead);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // Using CompletionHandler
        channel.read(buffer, 0, buffer,
            new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    System.out.println("Read completed: " + result + " bytes");
                    attachment.flip();
                    // Process buffer
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.err.println("Read failed: " + exc.getMessage());
                }
            });

        channel.close();
    }

    public void asyncWrite() throws IOException {
        Path path = Paths.get("async-output.txt");
        AsynchronousFileChannel channel = AsynchronousFileChannel.open(path,
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE);

        ByteBuffer buffer = ByteBuffer.wrap("Async content".getBytes());

        channel.write(buffer, 0, buffer,
            new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    System.out.println("Written: " + result + " bytes");
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.err.println("Write failed");
                }
            });
    }
}
```

### AsynchronousSocketChannel
```java
public class AsyncSocket {
    public void asyncClient() throws IOException, InterruptedException {
        AsynchronousSocketChannel channel = AsynchronousSocketChannel.open();

        // Connect asynchronously
        Future<Void> connectFuture =
            channel.connect(new InetSocketAddress("localhost", 8080));

        // Wait for connection
        connectFuture.get();

        // Write data
        ByteBuffer buffer = ByteBuffer.wrap("Hello Async".getBytes());
        Future<Integer> writeFuture = channel.write(buffer);
        writeFuture.get();

        // Read response
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        Future<Integer> readFuture = channel.read(readBuffer);
        Integer bytesRead = readFuture.get();

        readBuffer.flip();
        System.out.println("Received: " + new String(readBuffer.array(), 0, bytesRead));

        channel.close();
    }

    public void asyncServer() throws IOException {
        AsynchronousServerSocketChannel server =
            AsynchronousServerSocketChannel.open();
        server.bind(new InetSocketAddress(8080));

        server.accept(null,
            new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel client, Void attachment) {
                    // Accept next connection
                    server.accept(null, this);

                    // Handle client
                    handleClient(client);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    System.err.println("Accept failed");
                }
            });
    }

    private void handleClient(AsynchronousSocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        client.read(buffer, buffer,
            new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (result == -1) {
                        try {
                            client.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    attachment.flip();
                    client.write(attachment, attachment,
                        new CompletionHandler<Integer, ByteBuffer>() {
                            @Override
                            public void completed(Integer result, ByteBuffer buffer) {
                                buffer.clear();
                                // Read more
                                client.read(buffer, buffer, this);
                            }

                            @Override
                            public void failed(Throwable exc, ByteBuffer buffer) {
                                // Handle error
                            }
                        });
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    // Handle error
                }
            });
    }
}
```

## Memory-Mapped Files

### MappedByteBuffer
```java
public class MemoryMappedFile {
    public void memoryMap() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("large-file.dat", "rw");
             FileChannel channel = file.getChannel()) {

            // Map entire file
            MappedByteBuffer buffer = channel.map(
                FileChannel.MapMode.READ_WRITE, 0, channel.size());

            // Read from mapped buffer
            byte b = buffer.get(100);

            // Write to mapped buffer
            buffer.put(200, (byte) 42);

            // Force changes to disk
            buffer.force();

            // Load into physical memory
            buffer.load();

            // Check if loaded
            boolean isLoaded = buffer.isLoaded();
        }
    }

    public void mappedRegions() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("data.dat", "rw");
             FileChannel channel = file.getChannel()) {

            // Map specific region
            long position = 1000;
            long size = 4096;
            MappedByteBuffer region = channel.map(
                FileChannel.MapMode.READ_WRITE, position, size);

            // Multiple mapped regions
            MappedByteBuffer header = channel.map(
                FileChannel.MapMode.READ_ONLY, 0, 100);
            MappedByteBuffer data = channel.map(
                FileChannel.MapMode.READ_WRITE, 100, 1000);
        }
    }

    public void sharedMemory() throws IOException {
        // Process 1 - Writer
        try (RandomAccessFile file = new RandomAccessFile("shared.dat", "rw");
             FileChannel channel = file.getChannel()) {

            MappedByteBuffer buffer = channel.map(
                FileChannel.MapMode.READ_WRITE, 0, 1024);

            buffer.putInt(0, 42);
            buffer.force();
        }

        // Process 2 - Reader
        try (RandomAccessFile file = new RandomAccessFile("shared.dat", "r");
             FileChannel channel = file.getChannel()) {

            MappedByteBuffer buffer = channel.map(
                FileChannel.MapMode.READ_ONLY, 0, 1024);

            int value = buffer.getInt(0);
            System.out.println("Shared value: " + value);
        }
    }
}
```

## Character Sets and Encoding

### Charset Operations
```java
public class CharsetOperations {
    public void charsetBasics() {
        // Available charsets
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        charsets.forEach((name, charset) ->
            System.out.println(name + ": " + charset));

        // Default charset
        Charset defaultCharset = Charset.defaultCharset();

        // Get specific charset
        Charset utf8 = Charset.forName("UTF-8");
        Charset utf16 = StandardCharsets.UTF_16;

        // Charset properties
        System.out.println("Can encode: " + utf8.canEncode());
        System.out.println("Aliases: " + utf8.aliases());
    }

    public void encoding() {
        String text = "Hello 世界";
        Charset charset = StandardCharsets.UTF_8;

        // Encode string to bytes
        ByteBuffer buffer = charset.encode(text);
        byte[] bytes = text.getBytes(charset);

        // Using encoder
        CharsetEncoder encoder = charset.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        try {
            CharBuffer charBuffer = CharBuffer.wrap(text);
            ByteBuffer byteBuffer = encoder.encode(charBuffer);
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
    }

    public void decoding() {
        byte[] bytes = "Hello World".getBytes(StandardCharsets.UTF_8);
        Charset charset = StandardCharsets.UTF_8;

        // Decode bytes to string
        CharBuffer charBuffer = charset.decode(ByteBuffer.wrap(bytes));
        String text = new String(bytes, charset);

        // Using decoder
        CharsetDecoder decoder = charset.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.IGNORE);
        decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            CharBuffer result = decoder.decode(byteBuffer);
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
    }
}
```

## NIO vs Traditional I/O

### Comparison
```java
public class IOComparison {
    // Traditional I/O
    public void traditionalCopy(String from, String to) throws IOException {
        try (FileInputStream in = new FileInputStream(from);
             FileOutputStream out = new FileOutputStream(to)) {

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    // NIO
    public void nioCopy(String from, String to) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(from).getChannel();
             FileChannel destChannel = new FileOutputStream(to).getChannel()) {

            sourceChannel.transferTo(0, sourceChannel.size(), destChannel);
        }
    }

    // Traditional I/O server
    public void traditionalServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);

        while (true) {
            Socket client = serverSocket.accept(); // Blocks

            // Handle each client in separate thread
            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(client.getInputStream()));
                    PrintWriter writer = new PrintWriter(
                        client.getOutputStream(), true);

                    String line;
                    while ((line = reader.readLine()) != null) {
                        writer.println("Echo: " + line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    // NIO server
    public void nioServer() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Single thread handles multiple clients
        while (true) {
            selector.select();

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isAcceptable()) {
                    // Accept connection
                } else if (key.isReadable()) {
                    // Read data
                } else if (key.isWritable()) {
                    // Write data
                }
            }

            selector.selectedKeys().clear();
        }
    }
}
```

## Best Practices

### 1. Buffer Management
```java
public class BufferBestPractices {
    // Reuse buffers
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);

    public void reuseBuffer() {
        buffer.clear(); // Prepare for writing
        // Write data
        buffer.flip();  // Prepare for reading
        // Read data
    }

    // Direct vs Heap buffers
    public void chooseBufferType() {
        // Use direct buffers for large, long-lived buffers
        ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024 * 1024);

        // Use heap buffers for small, short-lived buffers
        ByteBuffer heapBuffer = ByteBuffer.allocate(1024);
    }

    // Buffer pooling
    class BufferPool {
        private final Queue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();
        private final int bufferSize;

        public BufferPool(int bufferSize, int poolSize) {
            this.bufferSize = bufferSize;
            for (int i = 0; i < poolSize; i++) {
                pool.offer(ByteBuffer.allocateDirect(bufferSize));
            }
        }

        public ByteBuffer acquire() {
            ByteBuffer buffer = pool.poll();
            if (buffer == null) {
                buffer = ByteBuffer.allocateDirect(bufferSize);
            }
            buffer.clear();
            return buffer;
        }

        public void release(ByteBuffer buffer) {
            buffer.clear();
            pool.offer(buffer);
        }
    }
}
```

### 2. Channel Best Practices
```java
public class ChannelBestPractices {
    // Always use try-with-resources
    public void properResourceManagement() throws IOException {
        try (FileChannel channel = FileChannel.open(
                Paths.get("file.txt"),
                StandardOpenOption.READ)) {
            // Use channel
        } // Auto-closed
    }

    // Scatter/Gather for efficient I/O
    public void scatterGather() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("data.txt", "r");
             FileChannel channel = file.getChannel()) {

            ByteBuffer header = ByteBuffer.allocate(128);
            ByteBuffer body = ByteBuffer.allocate(1024);
            ByteBuffer footer = ByteBuffer.allocate(128);

            ByteBuffer[] buffers = {header, body, footer};

            // Scatter read
            channel.read(buffers);

            // Gather write
            channel.write(buffers);
        }
    }
}
```

### 3. Selector Best Practices
```java
public class SelectorBestPractices {
    // Proper key handling
    public void handleKeys(Selector selector) throws IOException {
        selector.select();

        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iter = selectedKeys.iterator();

        while (iter.hasNext()) {
            SelectionKey key = iter.next();
            iter.remove(); // Always remove processed keys

            if (!key.isValid()) {
                continue;
            }

            try {
                if (key.isAcceptable()) {
                    // Handle accept
                } else if (key.isReadable()) {
                    // Handle read
                }
            } catch (IOException e) {
                key.cancel();
                key.channel().close();
            }
        }
    }
}
```

### 4. Performance Tips
```java
public class PerformanceTips {
    // Use memory-mapped files for large files
    public void efficientLargeFile() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile("large.dat", "r");
             FileChannel channel = file.getChannel()) {

            MappedByteBuffer buffer = channel.map(
                FileChannel.MapMode.READ_ONLY, 0, channel.size());
            // Process buffer
        }
    }

    // Batch operations
    public void batchWrites(List<String> lines, Path file) throws IOException {
        // Inefficient
        for (String line : lines) {
            Files.write(file, line.getBytes(), StandardOpenOption.APPEND);
        }

        // Efficient
        Files.write(file, lines);
    }

    // Use appropriate buffer sizes
    public ByteBuffer optimalBufferSize() {
        // For network I/O
        int networkBuffer = 64 * 1024; // 64KB

        // For file I/O
        int fileBuffer = 8 * 1024; // 8KB

        return ByteBuffer.allocate(networkBuffer);
    }
}
```

### 5. Error Handling
```java
public class ErrorHandling {
    public void robustFileOperation() {
        Path path = Paths.get("file.txt");

        try {
            // Check before operations
            if (!Files.exists(path)) {
                Files.createFile(path);
            }

            if (!Files.isWritable(path)) {
                throw new IOException("File not writable");
            }

            // Perform operations
            Files.write(path, "content".getBytes());

        } catch (NoSuchFileException e) {
            System.err.println("File not found: " + e.getFile());
        } catch (AccessDeniedException e) {
            System.err.println("Access denied: " + e.getFile());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        }
    }

    public void asyncErrorHandling() {
        AsynchronousFileChannel channel = null;
        try {
            channel = AsynchronousFileChannel.open(
                Paths.get("async.txt"), StandardOpenOption.READ);

            ByteBuffer buffer = ByteBuffer.allocate(1024);

            channel.read(buffer, 0, null,
                new CompletionHandler<Integer, Void>() {
                    @Override
                    public void completed(Integer result, Void attachment) {
                        // Handle success
                    }

                    @Override
                    public void failed(Throwable exc, Void attachment) {
                        if (exc instanceof AsynchronousCloseException) {
                            // Channel was closed
                        } else if (exc instanceof ClosedChannelException) {
                            // Channel already closed
                        } else {
                            // Other error
                        }
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## Common Use Cases

### 1. High-Performance File Processing
```java
public class FileProcessor {
    public void processLargeFile(Path input, Path output) throws IOException {
        try (FileChannel inChannel = FileChannel.open(input, StandardOpenOption.READ);
             FileChannel outChannel = FileChannel.open(output,
                StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

            // Direct transfer for best performance
            long size = inChannel.size();
            long position = 0;

            while (position < size) {
                position += inChannel.transferTo(position,
                    size - position, outChannel);
            }
        }
    }
}
```

### 2. Network Echo Server
```java
public class EchoServer {
    public void start() throws IOException {
        Selector selector = Selector.open();
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(8080));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer buffer = ByteBuffer.allocate(256);

        while (true) {
            selector.select();

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (key.isAcceptable()) {
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    buffer.clear();
                    int bytesRead = client.read(buffer);

                    if (bytesRead == -1) {
                        client.close();
                    } else {
                        buffer.flip();
                        client.write(buffer);
                    }
                }
            }
        }
    }
}
```

### 3. File Monitor Service
```java
public class FileMonitor {
    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;

    public FileMonitor() throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
    }

    public void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher,
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY);
        keys.put(key, dir);
    }

    public void processEvents() {
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) continue;

            for (WatchEvent<?> event : key.pollEvents()) {
                Path child = dir.resolve((Path) event.context());
                System.out.printf("%s: %s%n", event.kind(), child);
            }

            if (!key.reset()) {
                keys.remove(key);
            }
        }
    }
}
```

## Summary

Java NIO provides powerful, scalable I/O operations through:
- **Buffers** for efficient data handling
- **Channels** for bidirectional data transfer
- **Selectors** for multiplexed non-blocking I/O
- **Files API** for modern file operations
- **Asynchronous I/O** for concurrent operations
- **Memory-mapped files** for high-performance file access

Choose NIO when you need:
- High-performance I/O operations
- Non-blocking network servers
- Efficient file operations
- Scalable server applications
- Memory-mapped file access

[Back to Top](#table-of-contents)