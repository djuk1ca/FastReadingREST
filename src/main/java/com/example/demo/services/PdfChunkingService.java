package com.example.demo.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

@Service
public class PdfChunkingService {
	
	public static final int CHUNK_WORDS = 1200;
	private static final Pattern WS = Pattern.compile("\\s+");
	
	public List<List<String>> extractWordChunks(InputStream pdfStream) {
		String text = extractText(pdfStream);
		List<String> words = tokenize(text);
		return chunkWords(words, CHUNK_WORDS);
	}
	
	private String extractText(InputStream pdfStream) {
		try (RandomAccessRead rar = new RandomAccessReadBuffer(pdfStream);
		         PDDocument doc = Loader.loadPDF(rar)) {
			PDFTextStripper stripper = new PDFTextStripper();
			String raw = stripper.getText(doc);
			
			raw = raw.replace("\r\n", " ").replace("\n", " ").replace("\t", " ");
			raw = WS.matcher(raw).replaceAll(" ").trim();
			return raw;
		} catch(Exception e) {
			throw new RuntimeException("PDF parse failed: " + e.getMessage(), e);
		}
	}
	
	private List<String> tokenize(String normalized) {
		if(normalized == null || normalized.isBlank()) return List.of();
		String[] arr = WS.split(normalized.trim());
		List<String> out = new ArrayList<String>(arr.length);
		for(String w : arr) {
			if(!w.isBlank()) out.add(w);
		}
		return out;
	}
	
	private List<List<String>> chunkWords(List<String> words, int chunkSize) {
		List<List<String>> chunks = new ArrayList<>();
		for(int i = 0; i < words.size(); i += chunkSize) {
			int end = Math.min(i + chunkSize, words.size());
	        chunks.add(words.subList(i, end));
		}
		return chunks;
	}
}
