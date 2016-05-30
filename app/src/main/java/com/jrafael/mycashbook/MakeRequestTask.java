package com.jrafael.mycashbook;

import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.script.Script;
import com.google.api.services.script.model.ExecutionRequest;
import com.google.api.services.script.model.Operation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * An asynchronous task that handles the Google Apps Script Execution API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 *
 */
public class MakeRequestTask extends AsyncTask<Void, Void, Map<String, Object>> {
    private com.google.api.services.script.Script mService = null;
    private Exception mLastError = null;

    //TODO replace with your's sciptId
    String scriptId =
            "Mdxc3nmYupeD7rv2wAg50EBDBVl2wBwDj";

    AppScriptRequestHandler delegate;
    ExecutionRequest request;

    /**
     * This interface must be implemented by every  App Script function that'll call.
     * <p/>
     *
     */
    public interface AppScriptRequestHandler {
        /**
         * to set the request function's name, parameters, etc
         * @param request to use
         */
        void onPreExecute(ExecutionRequest request);
        /**
         * Positive result
         * @param response
         */
        void onPostExecute(java.util.Map<String, java.lang.Object> response);
        /**
         * Negative result
         * @param e with the reason of the fail
         */
        void onCancelled(Exception e);
    }

    /**
     * Extend the given HttpRequestInitializer (usually a credentials object)
     * with additional initialize() instructions.
     *
     * @param requestInitializer the initializer to copy and adjust; typically
     *         a credential object.
     * @return an initializer with an extended read timeout.
     */
    private static HttpRequestInitializer setHttpTimeout(
            final HttpRequestInitializer requestInitializer) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest)
                    throws java.io.IOException {
                requestInitializer.initialize(httpRequest);
                // This allows the API to call (and avoid timing out on)
                // functions that take up to 6 minutes to complete (the maximum
                // allowed script run time), plus a little overhead.
                httpRequest.setReadTimeout(380000);
            }
        };
    }

    /**
     * Interpret an error response returned by the API and return a String
     * summary.
     *
     * @param op the Operation returning an error response
     * @return summary of error response, or null if Operation returned no
     *     error
     */
    String getScriptError(Operation op) {
        if (op.getError() == null) {
            return null;
        }

        // Extract the first (and only) set of error details and cast as a Map.
        // The values of this map are the script's 'errorMessage' and
        // 'errorType', and an array of stack trace elements (which also need to
        // be cast as Maps).
        Map<String, Object> detail = op.getError().getDetails().get(0);
        List<Map<String, Object>> stacktrace =
                (List<Map<String, Object>>)detail.get("scriptStackTraceElements");

        java.lang.StringBuilder sb =
                new StringBuilder("\nScript error message: ");
        sb.append(detail.get("errorMessage"));

        if (stacktrace != null) {
            // There may not be a stacktrace if the script didn't start
            // executing.
            sb.append("\nScript error stacktrace:");
            for (Map<String, Object> elem : stacktrace) {
                sb.append("\n  ");
                sb.append(elem.get("function"));
                sb.append(":");
                sb.append(elem.get("lineNumber"));
            }
        }
        sb.append("\n");
        return sb.toString();
    }


    public MakeRequestTask(GoogleAccountCredential credential, AppScriptRequestHandler delegate) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.script.Script.Builder(
                transport, jsonFactory, setHttpTimeout(credential))
                .setApplicationName("Google Apps Script Execution API Android MyCashBook")
                .build();
        this.delegate = delegate;
    }



    /**
     * Background task to call Google Apps Script Execution API.
     * @param params no parameters needed for this task.
     */
    @Override
    protected java.util.Map<String, java.lang.Object> doInBackground(Void... params) {
        try {
            return getDataFromApi(mService);
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
    }

    private java.util.Map<String, java.lang.Object>  getDataFromApi(Script mService) throws IOException, GoogleAuthException {

        // Make the request.
        Operation op =
                mService.scripts().run(scriptId, request).execute();

        // Print results of request.
        if (op.getError() != null) {
            throw new IOException(getScriptError(op));
        }

        return op.getResponse();
    }


    @Override
    protected void onPreExecute() {
        request = new ExecutionRequest();
        delegate.onPreExecute(request);

    }

    @Override
    protected void onPostExecute(java.util.Map<String, java.lang.Object> output) {
        delegate.onPostExecute(output);
    }

    @Override
    protected void onCancelled() {
        delegate.onCancelled(mLastError);

    }
}
