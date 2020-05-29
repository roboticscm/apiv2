package vn.com.sky.upload;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.awt.List;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.buffer.DefaultDataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ZeroCopyHttpOutputMessage;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.Constants;
import vn.com.sky.base.GenericREST;
import vn.com.sky.util.FileUtil;
import vn.com.sky.util.MyServerResponse;
import vn.com.sky.util.StringUtil;

@Configuration
@AllArgsConstructor
public class UploadREST extends GenericREST {
	@Bean
    public RouterFunction<?> uploadRoutes() {
        return route(POST(buildURL("", this::upload)).and(RequestPredicates.accept(MediaType.MULTIPART_FORM_DATA)), this::upload)
        		.andRoute(GET(buildURL("", this::download)), this::download)
        		.andRoute(DELETE(buildURL("upload", this::delete)), this::delete);
    }
	
	private Mono<ServerResponse> download(ServerRequest request) {
		// SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE
        
        var filePath = getParam(request, "filePath");
        var saveDir = System.getProperty("user.dir");
        var file = new File(saveDir  + "/" + filePath );

        DefaultDataBuffer buffer = null;
		try {
			buffer = new DefaultDataBufferFactory().wrap(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		String fileName = file.getName();
		if(fileName.length() > Constants.UUID_LEN) {
			fileName = fileName.substring(Constants.UUID_LEN);
		}
		
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(BodyInserters.fromDataBuffers(buffer != null ? Flux.just(buffer) : Flux.empty()));
                
	}
	
	private Mono<ServerResponse> delete(ServerRequest request) {
		// SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE
        
        var filePath = getParam(request, "filePath");
        var saveDir = System.getProperty("user.dir");
        var file = new File(saveDir  + "/" + filePath );
        
        return ok(file.delete() ? "OK" : "Failed");
	}
	
	private Mono<ServerResponse> upload(ServerRequest request) {
		// SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE
        
        var savePath = getParam(request, "savePath");
        var fullPath = System.getProperty("user.dir") + "/" + savePath;
        FileUtil.createDir(fullPath);
        
        return request.body(BodyExtractors.toMultipartData())
        	.flatMap(parts -> {
        		var fileNames = new ArrayList<String>();
        		parts.forEach((k, v)->{
        			v.forEach(part -> {
        				FilePart filePart = (FilePart)part;
                		
                		var fileName = StringUtil.generateGUUID() + filePart.filename();
                		fileNames.add(fileName);
                		
                		var file = new File(fullPath  + "/" + fileName);
                		
                		
                		filePart.transferTo(file);
        			});
        		});
        		
        		return ok(fileNames, List.class);
        	});
	}
}
