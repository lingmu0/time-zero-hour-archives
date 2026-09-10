package net.xuwu.timevalidation;

import com.google.gson.*;
import java.nio.file.*;
import net.minecraft.gametest.framework.*;

/** Durable machine-readable report, while retaining vanilla's console diagnostics. */
public final class ValidationReporter implements TestReporter {
    private final JsonArray cases = new JsonArray();
    private final LogTestReporter log = new LogTestReporter();
    @Override public void onTestSuccess(GameTestInfo test) { log.onTestSuccess(test); add(test,true); }
    @Override public void onTestFailed(GameTestInfo test) { log.onTestFailed(test); add(test,false); }
    private void add(GameTestInfo test, boolean passed) {
        JsonObject entry=new JsonObject();entry.addProperty("name",test.getTestName());
        entry.addProperty("passed",passed);entry.addProperty("milliseconds",test.getRunTime());
        if(test.getError()!=null)entry.addProperty("error",test.getError().toString());cases.add(entry);
    }
    @Override public void finish() {
        JsonObject report=new JsonObject();report.addProperty("minecraft","1.21.1");report.addProperty("neoforge","21.1.233");
        report.addProperty("generated_at",java.time.Instant.now().toString());report.add("tests",cases);
        try {Path path=Path.of("../build/reports/runtime-gametests.json");Files.createDirectories(path.getParent());
            Files.writeString(path,new GsonBuilder().setPrettyPrinting().create().toJson(report));
        } catch(java.io.IOException e){throw new IllegalStateException("Cannot write test report",e);}
    }
}
