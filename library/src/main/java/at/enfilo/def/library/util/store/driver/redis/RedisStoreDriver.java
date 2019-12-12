//package at.enfilo.def.library.util.store.driver.redis;
//
//import at.enfilo.def.communication.dto.ServiceEndpointDTO;
//import at.enfilo.def.library.api.ILibraryStoreDriver;
//
//import javax.annotation.Nonnull;
//import java.io.*;
//import java.net.MalformedURLException;
//import java.net.URL;
//
///**
// * Created by mase on 20.04.2017.
// */
//public class RedisStoreDriver implements ILibraryStoreDriver {
//
//	private static final String URL_FORMAT = "%s/%s/%s.cache";
//    private static final int BUFFER_SIZE = 4194304;
//
//	@Override
//	public ILibraryStoreContext initStoreContext(URL url) {
//		return new ILibraryStoreContext() {
//			private InputStream inputStream;
//			private OutputStream outputStream;
//
//			@Override
//			public String getURL() {
//				return url.toString();
//			}
//
//            @Override
//            public boolean delete() throws IOException {
//				try (RedisClient client = new RedisClient(url.getHost(), url.getPort())) {
//					long numberOfRemovedItems = client.del(url.toString().getBytes());
//                	return numberOfRemovedItems > 0;
//				}
//            }
//
//            @Override
//			public InputStream getInputStream() throws IOException {
//				if (inputStream == null) {
//					inputStream = new JedisProgressiveInputStream(url);
//				}
//				return inputStream;
//			}
//
//			@Override
//			public OutputStream getOutputStream() throws IOException {
//				if (outputStream == null) {
//					outputStream = new JedisOutputStream(url);
//				}
//				return outputStream;
//			}
//		};
//	}
//
//	@Override
//	public ILibraryStoreContext initStoreContext(URL baseUrl, String path, String eId)
//    throws MalformedURLException {
//        final String url = String.format(URL_FORMAT, baseUrl.toString(), path, eId);
//        return initStoreContext(new URL(url));
//	}
//
//	public static class JedisProgressiveInputStream extends InputStream {
//        private final RedisClient client;
//        private ByteArrayInputStream byteBuffer;
//
//        private final byte[] key;
//
//        public JedisProgressiveInputStream(@Nonnull URL url)
//        throws IOException {
//            this.client = new RedisClient(
//                url.getHost(),
//                url.getPort()
//            );
//
//            this.key = url.toString().getBytes();
//        }
//
//        @Override
//        public int read()
//        throws IOException {
//            return getByteBuffer().read();
//        }
//
//        @Override
//        public int available()
//        throws IOException {
//            return getByteBuffer().available();
//        }
//
//        @Override
//        public void close()
//        throws IOException {
//            if (byteBuffer != null) {
//                byteBuffer.close();
//            }
//            client.close();
//        }
//
//        private ByteArrayInputStream getByteBuffer()
//        throws IOException {
//            if (byteBuffer == null) {
//                byteBuffer = new ByteArrayInputStream(
//                    client.get(key)
//                );
//            }
//            return byteBuffer;
//        }
//    }
//
//    public static class JedisChunkInputStream extends InputStream {
//
//        private final RedisClient client;
//        private ByteArrayInputStream byteBuffer;
//
//        private final byte[] key;
//        private final long size;
//        private int offset;
//
//        public JedisChunkInputStream(@Nonnull ServiceEndpointDTO cacheEndpoint, String url)
//        throws IOException {
//            this.client = new RedisClient(
//                cacheEndpoint.getHost(),
//                cacheEndpoint.getPort()
//            );
//
//            this.key = url.getBytes();
//            this.size = client.strlen(key);
//            this.offset = 0;
//        }
//
//        @Override
//        public int read()
//        throws IOException {
//            if (byteBuffer == null || byteBuffer.available() == 0) {
//                int endOffset = offset + (BUFFER_SIZE - 1) < size ? offset + (BUFFER_SIZE - 1) : -1;
//
//                byte[] data = client.getrange(key, offset, endOffset);
//                offset = endOffset != -1 ? endOffset + 1 : (int) size;
//
//                byteBuffer = new ByteArrayInputStream(data);
//            }
//
//            return byteBuffer.read();
//        }
//
//        @Override
//		public int available()
//		throws IOException {
//            if (byteBuffer != null && byteBuffer.available() > 0) return byteBuffer.available();
//			return Math.min(BUFFER_SIZE, (int) size - offset);
//		}
//
//        @Override
//        public void close()
//        throws IOException {
//            byteBuffer.close();
//            client.close();
//        }
//    }
//
//	public static class JedisOutputStream extends OutputStream {
//
//	    private final RedisClient client;
//        private final ByteArrayOutputStream byteBuffer;
//
//        private final byte[] key;
//	    private int offset;
//
//        public JedisOutputStream(@Nonnull URL url)
//        throws IOException {
//            this.client = new RedisClient(
//                url.getHost(),
//                url.getPort()
//            );
//
//            this.byteBuffer = new ByteArrayOutputStream(BUFFER_SIZE);
//
//            this.key = url.toString().getBytes();
//            this.offset = 0;
//        }
//
//        @Override
//        public void write(int b)
//        throws IOException {
//            if (byteBuffer.size() == BUFFER_SIZE) flush();
//            byteBuffer.write(b);
//        }
//
//        @Override
//        public void flush()
//        throws IOException {
//            if (offset == 0 && byteBuffer.size() > 0) client.del(key);
//            offset = (int) client.setrange(key, offset, byteBuffer.toByteArray());
//
//            byteBuffer.reset();
//        }
//
//        @Override
//        public void close()
//        throws IOException {
//            flush();
//
//            byteBuffer.close();
//            client.close();
//        }
//    }
//}
