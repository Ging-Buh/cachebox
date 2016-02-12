package CB_Core;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;

public abstract class AbortableHttpRequestBase {
	HttpRequestBase request;

	public AbortableHttpRequestBase(HttpPost httppost) {

	}

	public abstract boolean abort();
}
