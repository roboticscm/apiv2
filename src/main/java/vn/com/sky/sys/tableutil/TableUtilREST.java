package vn.com.sky.sys.tableutil;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.badRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import vn.com.sky.Message;
import vn.com.sky.base.GenericREST;
import vn.com.sky.util.MyServerResponse;
import vn.com.sky.util.QueryUtil;
import vn.com.sky.util.SDate;

@Configuration
@AllArgsConstructor
public class TableUtilREST extends GenericREST {
    private CustomTableUtilRepo customRepo;

    @Bean
    public RouterFunction<?> tableUtilRoutes() {
        return route(GET(buildURL("table-util", this::hasAnyDeletedRecord)), this::hasAnyDeletedRecord)
            .andRoute(GET(buildURL("table-util", this::getAllDeletedRecords)), this::getAllDeletedRecords)
            .andRoute(GET(buildURL("table-util", this::getSimpleList)), this::getSimpleList)
            .andRoute(GET(buildURL("table-util", this::getOneById)), this::getOneById)
            .andRoute(GET(buildURL("table-util", this::getAllColumnsOfTable)), this::getAllColumnsOfTable)
            .andRoute(GET(buildURL("table-util", this::jsonQuery)), this::jsonQuery)
            .andRoute(PUT(buildURL("table-util", this::updateTableById)), this::updateTableById)
            .andRoute(DELETE(buildURL("table-util", this::softDeleteMany)), this::softDeleteMany)
            .andRoute(DELETE(buildURL("table-util", this::restoreOrForeverDelete)), this::restoreOrForeverDelete);
    }

    private Mono<ServerResponse> hasAnyDeletedRecord(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var tableName = getParam(request, "tableName");
        var onlyMeStr = getParam(request, "onlyMe", "false");

        if (tableName == null || "null".equals(tableName)) return error("tableName", "SYS.MSG.INVILID_TABLE_NAME");

        var onlyMe = false;
        try {
            onlyMe = Boolean.parseBoolean(onlyMeStr);
        } catch (Exception e) {
            e.printStackTrace();
            return error("onlyMe", "SYS.MSG.INVILID_ONLY_ME");
        }

        return customRepo
            .hasAnyDeletedRecord(tableName, onlyMe, getUserId(request))
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> restoreOrForeverDelete(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var tableName = getParam(request, "tableName");
        var deleteIds = getParam(request, "deleteIds");
        var restoreIds = getParam(request, "restoreIds");

        if (tableName == null || "null".equals(tableName)) return error("tableName", "SYS.MSG.INVILID_TABLE_NAME");

        if ((deleteIds != null && deleteIds.trim().length() == 0) || "null".equals(deleteIds)) deleteIds = null;

        if ((restoreIds != null && restoreIds.trim().length() == 0) || "null".equals(restoreIds)) restoreIds = null;

        return customRepo
            .restoreOrForeverDelete(tableName, deleteIds, restoreIds, getUserId(request), SDate.now())
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> getAllDeletedRecords(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var tableName = getParam(request, "tableName");
        var columns = getParam(request, "columns");
        var onlyMeStr = getParam(request, "onlyMe", "false");

        if (tableName == null || "null".equals(tableName)) return error("tableName", "SYS.MSG.INVILID_TABLE_NAME");

        var onlyMe = false;
        try {
            onlyMe = Boolean.parseBoolean(onlyMeStr);
        } catch (Exception e) {
            e.printStackTrace();
            return error("onlyMe", "SYS.MSG.INVILID_ONLY_ME");
        }

        return customRepo
            .getAllDeletedRecords(tableName, columns, onlyMe, getUserId(request))
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> getSimpleList(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var tableName = getParam(request, "tableName");
        var columns = getParam(request, "columns");
        var orderBy = getParam(request, "orderBy");

        if (tableName == null || "null".equals(tableName)) return badRequest().bodyValue(Message.INVALID_TABLE_NAME);

        Long page, pageSize;
        try {
            page = getLongParam(request, "page", 1L);
            pageSize = getLongParam(request, "pageSize", -1L);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(Message.INVALID_PAGINATION);
        }

        Boolean onlyMe, includeDisabled;
        try {
            onlyMe = getBoolParam(request, "onlyMe", false);
            includeDisabled = getBoolParam(request, "includeDisabled", true);
        } catch (Exception e) {
            e.printStackTrace();
            return badRequest().bodyValue(e);
        }

        return customRepo
            .getSimpleList(tableName, columns, orderBy, page, pageSize, onlyMe, getUserId(request), includeDisabled)
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> getOneById(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var tableName = getParam(request, "tableName");
        Long id;
        try {
            id = getLongParam(request, "id");
            System.out.println("id " + id);
        } catch (Exception e1) {
            return badRequest().bodyValue(Message.INVALID_ID);
        }

        if (tableName == null || "null".equals(tableName)) return badRequest().bodyValue(Message.INVALID_TABLE_NAME);

        return customRepo.getOneById(tableName, id).flatMap(item -> ok(item)).onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> softDeleteMany(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var tableName = getParam(request, "tableName");
        var deletedIds = getParam(request, "deletedIds");

        if (tableName == null || "null".equals(tableName)) return badRequest().bodyValue(Message.INVALID_TABLE_NAME);

        if (deletedIds == null || "null".equals(deletedIds)) return badRequest().bodyValue(Message.INVALID_ID);

        return customRepo
            .softDeleteMany(tableName, deletedIds, getUserId(request), SDate.now())
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> getAllColumnsOfTable(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var tableName = getParam(request, "tableName");

        return customRepo.getAllColumnsOfTable(tableName).flatMap(item -> ok(item)).onErrorResume(e -> error(e));
    }
    
    
    private Mono<ServerResponse> jsonQuery(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var query = getParam(request, "query");
        
        System.out.println(query);
        
        if(!QueryUtil.isValiad(query)) {
        	return error("SYS.MSG.INVALID_QUERY");
        }

        return customRepo.jsonQuery(query).flatMap(item -> ok(item)).onErrorResume(e -> error(e));
    }

    private Mono<ServerResponse> updateTableById(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        var tableName = getParam(request, "tableName");
        var expression = getParam(request, "expression");
        Long id;
        try {
            id = getLongParam(request, "id");
        } catch (Exception e1) {
            return badRequest().bodyValue(Message.INVALID_ID);
        }

        if (tableName == null || "null".equals(tableName)) return badRequest().bodyValue(Message.INVALID_TABLE_NAME);
        if (expression == null || "null".equals(expression)) return badRequest().bodyValue(Message.INVALID_EXPRESSION);

        return customRepo
            .updateTableById(tableName, expression, id, getUserId(request), SDate.now())
            .flatMap(item -> ok(item))
            .onErrorResume(e -> error(e));
    }
}
