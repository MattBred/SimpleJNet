
import java.io.InputStream;

public interface SimpleJNetBaseWrapper {

	void error(String errorString);
	void onConnected();
	void onConnectionClosed();
	void onConnectionFailed(String reason);
	void connect();
	void disconnect();
	void setPort(int port);
	void setUseSSL(boolean useSSL);
	void setRequireSSLClientAuth(boolean requireClientAuth);
	void setKeyStore(InputStream keyStorePath, String keyStorePassword);
	void setTrustStore(InputStream trustStorePath, String trustStorePassword);
}
