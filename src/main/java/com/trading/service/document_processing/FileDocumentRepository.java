package com.trading.service.document_processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Append-only NDJSON repository for financial documents. */
public class FileDocumentRepository implements DocumentRepository {
  private final Path file;
  private final ObjectMapper mapper = new ObjectMapper();
  private BufferedWriter writer;

  public FileDocumentRepository(Path file) throws IOException {
    this.file = file;
    Files.createDirectories(file.getParent());
    if (!Files.exists(file)) Files.createFile(file);
    this.writer =
        Files.newBufferedWriter(
            file, java.nio.charset.StandardCharsets.UTF_8, java.nio.file.StandardOpenOption.APPEND);
  }

  @Override
  public synchronized void save(FinancialDocument doc) throws IOException {
    ObjectNode node = mapper.createObjectNode();
    node.put("id", doc.id());
    node.put("symbol", doc.symbol());
    node.put("type", doc.type().name());
    node.put("publishedAt", doc.publishedAt().toString());
    node.put("source", doc.source());
    node.put("rawText", doc.rawText());
    writer.write(mapper.writeValueAsString(node));
    writer.newLine();
    writer.flush();
  }

  @Override
  public List<FinancialDocument> findBySymbol(String symbol) throws IOException {
    List<FinancialDocument> result = new ArrayList<>();
    try (BufferedReader br = Files.newBufferedReader(file)) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isEmpty()) continue;
        ObjectNode n = (ObjectNode) mapper.readTree(line);
        if (!symbol.equals(n.get("symbol").asText())) continue;
        FinancialDocument doc =
            new FinancialDocument(
                n.get("id").asText(),
                n.get("symbol").asText(),
                DocumentType.valueOf(n.get("type").asText()),
                Instant.parse(n.get("publishedAt").asText()),
                n.get("source").asText(),
                n.get("rawText").asText());
        result.add(doc);
      }
    }
    return result;
  }

  @Override
  public void close() throws IOException {
    if (writer != null) writer.close();
  }
}
