package sourceconnector.domain;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DefaultBatchLogs implements BatchMessages {
  private final List<String> logs;

  public void add(String log) {
    this.logs.add(log);
  }
  public void addAll(List<String> logs) {
    this.logs.addAll(logs);
  }

  @Override
  public List<String> get() {
    if (logs.isEmpty()) {
      throw new IllegalStateException("No log found");
    }
    return this.logs;
  }
}
