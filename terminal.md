ar:7.1.8.Final]
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:201) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:100) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.executeQuery(JdbcSelectExecutor.java:64) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:138) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.lambda$new$1(ConcreteSqmSelectQueryPlan.java:146) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.withCacheableSqmInterpretation(ConcreteSqmSelectQueryPlan.java:455) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.performList(ConcreteSqmSelectQueryPlan.java:388) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.sqm.internal.SqmQueryImpl.doList(SqmQueryImpl.java:386) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.spi.AbstractSelectionQuery.list(AbstractSelectionQuery.java:154) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.Query.getResultList(Query.java:121) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.springframework.data.jpa.repository.query.JpaQueryExecution$CollectionExecution.doExecute(JpaQueryExecution.java:132) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.data.jpa.repository.query.JpaQueryExecution.execute(JpaQueryExecution.java:99) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.data.jpa.repository.query.AbstractJpaQuery.doExecute(AbstractJpaQuery.java:164) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.data.jpa.repository.query.AbstractJpaQuery.execute(AbstractJpaQuery.java:154) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:169) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:158) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:167) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:146) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(DefaultMethodInvokingMethodInterceptor.java:69) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:135) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor$CrudMethodMetadataPopulatingMethodInterceptor.invoke(CrudMethodMetadataPostProcessor.java:137) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:222) ~[spring-aop-7.0.1.jar:7.0.1]
        at jdk.proxy2/jdk.proxy2.$Proxy159.findByHotelId(Unknown Source) ~[na:na]
        at com.hotel.hotel.controller.BookingController.getBookingsByHotel(BookingController.java:121) ~[classes/:na]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:190) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:934) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:853) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:86) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1003) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892) ~[spring-webmvc-7.0.1.jar:7.0.1]   
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622) ~[tomcat-embed-core-11.0.14.jar:6.1]
        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874) ~[spring-webmvc-7.0.1.jar:7.0.1] 
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710) ~[tomcat-embed-core-11.0.14.jar:6.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-11.0.14.jar:11.0.14] 
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341) ~[tomcat-embed-core-11.0.14.jar:11.0.14] 
        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397) ~[tomcat-embed-core-11.0.14.jar:11.0.14]  
        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1778) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

2025-12-27T12:45:34.568+08:00 DEBUG 14168 --- [hotel] [0.0-8082-exec-9] org.hibernate.SQL                        : 
    select
        b1_0.id,
        b1_0.actual_room_type,
        b1_0.adr,
        b1_0.adults,
        b1_0.assigned_room_id,
        b1_0.babies,
        b1_0.booking_changes,
        b1_0.booking_date,
        b1_0.booking_number,
        b1_0.cancel_date,
        b1_0.check_in_date,
        b1_0.check_out_date,
        b1_0.children,
        b1_0.created_at,
        b1_0.customer_id,
        b1_0.days_in_waiting_list,
        b1_0.deposit_type,
        b1_0.distribution_channel,
        b1_0.hotel_id,
        b1_0.is_canceled,
        b1_0.lead_time,
        b1_0.market_segment,
        b1_0.meal_type,
        b1_0.requests_text,
        b1_0.required_car_parking_spaces,
        b1_0.room_type_id,
        b1_0.special_requests,
        b1_0.status,
        b1_0.stays_in_week_nights,
        b1_0.stays_in_weekend_nights,
        b1_0.total_nights,
        b1_0.total_price,
        b1_0.updated_at
    from
        bookings b1_0
    where
        b1_0.id=?
Hibernate: 
    select
        b1_0.id,
        b1_0.actual_room_type,
        b1_0.adr,
        b1_0.adults,
        b1_0.assigned_room_id,
        b1_0.babies,
        b1_0.booking_changes,
        b1_0.booking_date,
        b1_0.booking_number,
        b1_0.cancel_date,
        b1_0.check_in_date,
        b1_0.check_out_date,
        b1_0.children,
        b1_0.created_at,
        b1_0.customer_id,
        b1_0.days_in_waiting_list,
        b1_0.deposit_type,
        b1_0.distribution_channel,
        b1_0.hotel_id,
        b1_0.is_canceled,
        b1_0.lead_time,
        b1_0.market_segment,
        b1_0.meal_type,
        b1_0.requests_text,
        b1_0.required_car_parking_spaces,
        b1_0.room_type_id,
        b1_0.special_requests,
        b1_0.status,
        b1_0.stays_in_week_nights,
        b1_0.stays_in_weekend_nights,
        b1_0.total_nights,
        b1_0.total_price,
        b1_0.updated_at
    from
        bookings b1_0
    where
        b1_0.id=?
2025-12-27T12:45:34.646+08:00 ERROR 14168 --- [hotel] [0.0-8082-exec-9] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.dao.InvalidDataAccessApiUsageException: No enum constant com.hotel.hotel.entity.Booking.MealType.bb] with root cause

java.lang.IllegalArgumentException: No enum constant com.hotel.hotel.entity.Booking.MealType.bb
        at java.base/java.lang.Enum.valueOf(Enum.java:293) ~[na:na]
        at org.hibernate.type.descriptor.java.EnumJavaType.fromName(EnumJavaType.java:255) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.java.EnumJavaType.wrap(EnumJavaType.java:128) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.java.EnumJavaType.wrap(EnumJavaType.java:38) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.jdbc.EnumJdbcType$2.doExtract(EnumJdbcType.java:88) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.jdbc.BasicExtractor.extract(BasicExtractor.java:42) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.jdbc.internal.JdbcValuesResultSetImpl.getCurrentRowValue(JdbcValuesResultSetImpl.java:385) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.RowProcessingStateStandardImpl.getJdbcValue(RowProcessingStateStandardImpl.java:149) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.basic.BasicResultAssembler.extractRawValue(BasicResultAssembler.java:52) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.basic.BasicResultAssembler.assemble(BasicResultAssembler.java:58) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.extractConcreteTypeStateValues(EntityInitializerImpl.java:1635) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeEntityInstance(EntityInitializerImpl.java:1353) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeInstance(EntityInitializerImpl.java:1331) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeInstance(EntityInitializerImpl.java:95) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.StandardRowReader.coordinateInitializers(StandardRowReader.java:239) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.StandardRowReader.readRow(StandardRowReader.java:136) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.readUniqueAssert(ListResultsConsumer.java:258) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.readRows(ListResultsConsumer.java:232) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:166) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:33) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:201) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:100) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.executeQuery(JdbcSelectExecutor.java:64) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:138) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.ast.internal.SingleIdLoadPlan.load(SingleIdLoadPlan.java:143) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.ast.internal.SingleIdLoadPlan.load(SingleIdLoadPlan.java:115) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.ast.internal.SingleIdEntityLoaderStandardImpl.load(SingleIdEntityLoaderStandardImpl.java:66) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.persister.entity.AbstractEntityPersister.doLoad(AbstractEntityPersister.java:3529) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.persister.entity.AbstractEntityPersister.load(AbstractEntityPersister.java:3518) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.loadFromDatasource(DefaultLoadEventListener.java:596) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.loadFromCacheOrDatasource(DefaultLoadEventListener.java:570) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.load(DefaultLoadEventListener.java:554) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.doLoad(DefaultLoadEventListener.java:538) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.load(DefaultLoadEventListener.java:204) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.loadWithRegularProxy(DefaultLoadEventListener.java:284) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.proxyOrLoad(DefaultLoadEventListener.java:236) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.doOnLoad(DefaultLoadEventListener.java:109) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.onLoad(DefaultLoadEventListener.java:68) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.service.internal.EventListenerGroupImpl.fireEventOnEachListener(EventListenerGroupImpl.java:151) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.internal.SessionImpl.fireLoadNoChecks(SessionImpl.java:1291) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.internal.SessionImpl.fireLoad(SessionImpl.java:1278) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]     
        at org.hibernate.internal.SessionImpl.load(SessionImpl.java:1260) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.doLoad(IdentifierLoadAccessImpl.java:181) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.lambda$load$1(IdentifierLoadAccessImpl.java:167) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.perform(IdentifierLoadAccessImpl.java:134) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.load(IdentifierLoadAccessImpl.java:167) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.internal.SessionImpl.find(SessionImpl.java:2407) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.internal.SessionImpl.find(SessionImpl.java:2381) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.orm.jpa.ExtendedEntityManagerCreator$ExtendedEntityManagerInvocationHandler.invoke(ExtendedEntityManagerCreator.java:362) ~[spring-orm-7.0.1.jar:7.0.1]
        at jdk.proxy2/jdk.proxy2.$Proxy146.find(Unknown Source) ~[na:na]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.orm.jpa.SharedEntityManagerCreator$SharedEntityManagerInvocationHandler.invoke(SharedEntityManagerCreator.java:317) ~[spring-orm-7.0.1.jar:7.0.1]
        at jdk.proxy2/jdk.proxy2.$Proxy146.find(Unknown Source) ~[na:na]
        at org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById(SimpleJpaRepository.java:344) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker$RepositoryFragmentMethodInvoker.lambda$new$0(RepositoryMethodInvoker.java:278) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:169) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:158) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryComposition$RepositoryFragments.invoke(RepositoryComposition.java:545) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryComposition.invoke(RepositoryComposition.java:290) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryFactorySupport$ImplementationMethodExecutionInterceptor.invoke(RepositoryFactorySupport.java:708) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:171) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:146) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(DefaultMethodInvokingMethodInterceptor.java:69) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:135) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor$CrudMethodMetadataPopulatingMethodInterceptor.invoke(CrudMethodMetadataPostProcessor.java:166) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:222) ~[spring-aop-7.0.1.jar:7.0.1]
        at jdk.proxy2/jdk.proxy2.$Proxy159.findById(Unknown Source) ~[na:na]
        at com.hotel.hotel.controller.BookingController.getBookingById(BookingController.java:164) ~[classes/:na]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:190) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:934) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:853) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:86) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1003) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892) ~[spring-webmvc-7.0.1.jar:7.0.1]   
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622) ~[tomcat-embed-core-11.0.14.jar:6.1]
        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874) ~[spring-webmvc-7.0.1.jar:7.0.1] 
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710) ~[tomcat-embed-core-11.0.14.jar:6.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-11.0.14.jar:11.0.14] 
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341) ~[tomcat-embed-core-11.0.14.jar:11.0.14] 
        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397) ~[tomcat-embed-core-11.0.14.jar:11.0.14]  
        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1778) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

2025-12-27T12:45:44.028+08:00 DEBUG 14168 --- [hotel] [.0-8082-exec-10] org.hibernate.SQL                        : 
    select
        b1_0.id,
        b1_0.actual_room_type,
        b1_0.adr,
        b1_0.adults,
        b1_0.assigned_room_id,
        b1_0.babies,
        b1_0.booking_changes,
        b1_0.booking_date,
        b1_0.booking_number,
        b1_0.cancel_date,
        b1_0.check_in_date,
        b1_0.check_out_date,
        b1_0.children,
        b1_0.created_at,
        b1_0.customer_id,
        b1_0.days_in_waiting_list,
        b1_0.deposit_type,
        b1_0.distribution_channel,
        b1_0.hotel_id,
        b1_0.is_canceled,
        b1_0.lead_time,
        b1_0.market_segment,
        b1_0.meal_type,
        b1_0.requests_text,
        b1_0.required_car_parking_spaces,
        b1_0.room_type_id,
        b1_0.special_requests,
        b1_0.status,
        b1_0.stays_in_week_nights,
        b1_0.stays_in_weekend_nights,
        b1_0.total_nights,
        b1_0.total_price,
        b1_0.updated_at
    from
        bookings b1_0
    where
        b1_0.booking_number=?
Hibernate: 
    select
        b1_0.id,
        b1_0.actual_room_type,
        b1_0.adr,
        b1_0.adults,
        b1_0.assigned_room_id,
        b1_0.babies,
        b1_0.booking_changes,
        b1_0.booking_date,
        b1_0.booking_number,
        b1_0.cancel_date,
        b1_0.check_in_date,
        b1_0.check_out_date,
        b1_0.children,
        b1_0.created_at,
        b1_0.customer_id,
        b1_0.days_in_waiting_list,
        b1_0.deposit_type,
        b1_0.distribution_channel,
        b1_0.hotel_id,
        b1_0.is_canceled,
        b1_0.lead_time,
        b1_0.market_segment,
        b1_0.meal_type,
        b1_0.requests_text,
        b1_0.required_car_parking_spaces,
        b1_0.room_type_id,
        b1_0.special_requests,
        b1_0.status,
        b1_0.stays_in_week_nights,
        b1_0.stays_in_weekend_nights,
        b1_0.total_nights,
        b1_0.total_price,
        b1_0.updated_at
    from
        bookings b1_0
    where
        b1_0.booking_number=?
2025-12-27T12:45:44.055+08:00 ERROR 14168 --- [hotel] [.0-8082-exec-10] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.dao.InvalidDataAccessApiUsageException: No enum constant com.hotel.hotel.entity.Booking.MealType.bb] with root cause

java.lang.IllegalArgumentException: No enum constant com.hotel.hotel.entity.Booking.MealType.bb
        at java.base/java.lang.Enum.valueOf(Enum.java:293) ~[na:na]
        at org.hibernate.type.descriptor.java.EnumJavaType.fromName(EnumJavaType.java:255) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.java.EnumJavaType.wrap(EnumJavaType.java:128) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.java.EnumJavaType.wrap(EnumJavaType.java:38) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.jdbc.EnumJdbcType$2.doExtract(EnumJdbcType.java:88) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.jdbc.BasicExtractor.extract(BasicExtractor.java:42) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.jdbc.internal.JdbcValuesResultSetImpl.getCurrentRowValue(JdbcValuesResultSetImpl.java:385) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.RowProcessingStateStandardImpl.getJdbcValue(RowProcessingStateStandardImpl.java:149) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.basic.BasicResultAssembler.extractRawValue(BasicResultAssembler.java:52) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.basic.BasicResultAssembler.assemble(BasicResultAssembler.java:58) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.extractConcreteTypeStateValues(EntityInitializerImpl.java:1635) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeEntityInstance(EntityInitializerImpl.java:1353) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeInstance(EntityInitializerImpl.java:1331) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeInstance(EntityInitializerImpl.java:95) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.StandardRowReader.coordinateInitializers(StandardRowReader.java:239) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.StandardRowReader.readRow(StandardRowReader.java:136) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.read(ListResultsConsumer.java:245) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.readRows(ListResultsConsumer.java:235) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:166) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:33) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:201) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:100) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.executeQuery(JdbcSelectExecutor.java:64) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:138) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.lambda$new$1(ConcreteSqmSelectQueryPlan.java:146) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.withCacheableSqmInterpretation(ConcreteSqmSelectQueryPlan.java:455) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.sqm.internal.ConcreteSqmSelectQueryPlan.performList(ConcreteSqmSelectQueryPlan.java:388) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.sqm.internal.SqmQueryImpl.doList(SqmQueryImpl.java:386) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.spi.AbstractSelectionQuery.list(AbstractSelectionQuery.java:154) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.query.spi.AbstractSelectionQuery.getSingleResultOrNull(AbstractSelectionQuery.java:320) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.springframework.data.jpa.repository.query.JpaQueryExecution$SingleEntityExecution.doExecute(JpaQueryExecution.java:328) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.data.jpa.repository.query.JpaQueryExecution.execute(JpaQueryExecution.java:99) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.data.jpa.repository.query.AbstractJpaQuery.doExecute(AbstractJpaQuery.java:164) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.data.jpa.repository.query.AbstractJpaQuery.execute(AbstractJpaQuery.java:154) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:169) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:158) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:167) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:146) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(DefaultMethodInvokingMethodInterceptor.java:69) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:135) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor$CrudMethodMetadataPopulatingMethodInterceptor.invoke(CrudMethodMetadataPostProcessor.java:137) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:222) ~[spring-aop-7.0.1.jar:7.0.1]
        at jdk.proxy2/jdk.proxy2.$Proxy159.findByBookingNumber(Unknown Source) ~[na:na]
        at com.hotel.hotel.controller.BookingController.getBookingByNumber(BookingController.java:177) ~[classes/:na]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:190) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:934) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:853) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:86) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1003) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892) ~[spring-webmvc-7.0.1.jar:7.0.1]   
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622) ~[tomcat-embed-core-11.0.14.jar:6.1]
        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874) ~[spring-webmvc-7.0.1.jar:7.0.1] 
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710) ~[tomcat-embed-core-11.0.14.jar:6.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-11.0.14.jar:11.0.14] 
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341) ~[tomcat-embed-core-11.0.14.jar:11.0.14] 
        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397) ~[tomcat-embed-core-11.0.14.jar:11.0.14]  
        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1778) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

2025-12-27T12:46:11.830+08:00 DEBUG 14168 --- [hotel] [0.0-8082-exec-1] org.hibernate.SQL                        : 
    select
        b1_0.id,
        b1_0.actual_room_type,
        b1_0.adr,
        b1_0.adults,
        b1_0.assigned_room_id,
        b1_0.babies,
        b1_0.booking_changes,
        b1_0.booking_date,
        b1_0.booking_number,
        b1_0.cancel_date,
        b1_0.check_in_date,
        b1_0.check_out_date,
        b1_0.children,
        b1_0.created_at,
        b1_0.customer_id,
        b1_0.days_in_waiting_list,
        b1_0.deposit_type,
        b1_0.distribution_channel,
        b1_0.hotel_id,
        b1_0.is_canceled,
        b1_0.lead_time,
        b1_0.market_segment,
        b1_0.meal_type,
        b1_0.requests_text,
        b1_0.required_car_parking_spaces,
        b1_0.room_type_id,
        b1_0.special_requests,
        b1_0.status,
        b1_0.stays_in_week_nights,
        b1_0.stays_in_weekend_nights,
        b1_0.total_nights,
        b1_0.total_price,
        b1_0.updated_at
    from
        bookings b1_0
    where
        b1_0.id=?
Hibernate: 
    select
        b1_0.id,
        b1_0.actual_room_type,
        b1_0.adr,
        b1_0.adults,
        b1_0.assigned_room_id,
        b1_0.babies,
        b1_0.booking_changes,
        b1_0.booking_date,
        b1_0.booking_number,
        b1_0.cancel_date,
        b1_0.check_in_date,
        b1_0.check_out_date,
        b1_0.children,
        b1_0.created_at,
        b1_0.customer_id,
        b1_0.days_in_waiting_list,
        b1_0.deposit_type,
        b1_0.distribution_channel,
        b1_0.hotel_id,
        b1_0.is_canceled,
        b1_0.lead_time,
        b1_0.market_segment,
        b1_0.meal_type,
        b1_0.requests_text,
        b1_0.required_car_parking_spaces,
        b1_0.room_type_id,
        b1_0.special_requests,
        b1_0.status,
        b1_0.stays_in_week_nights,
        b1_0.stays_in_weekend_nights,
        b1_0.total_nights,
        b1_0.total_price,
        b1_0.updated_at
    from
        bookings b1_0
    where
        b1_0.id=?
2025-12-27T12:46:11.886+08:00 ERROR 14168 --- [hotel] [0.0-8082-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: org.springframework.dao.InvalidDataAccessApiUsageException: No enum constant com.hotel.hotel.entity.Booking.MealType.bb] with root cause

java.lang.IllegalArgumentException: No enum constant com.hotel.hotel.entity.Booking.MealType.bb
        at java.base/java.lang.Enum.valueOf(Enum.java:293) ~[na:na]
        at org.hibernate.type.descriptor.java.EnumJavaType.fromName(EnumJavaType.java:255) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.java.EnumJavaType.wrap(EnumJavaType.java:128) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.java.EnumJavaType.wrap(EnumJavaType.java:38) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.jdbc.EnumJdbcType$2.doExtract(EnumJdbcType.java:88) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.type.descriptor.jdbc.BasicExtractor.extract(BasicExtractor.java:42) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.jdbc.internal.JdbcValuesResultSetImpl.getCurrentRowValue(JdbcValuesResultSetImpl.java:385) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.RowProcessingStateStandardImpl.getJdbcValue(RowProcessingStateStandardImpl.java:149) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.basic.BasicResultAssembler.extractRawValue(BasicResultAssembler.java:52) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.basic.BasicResultAssembler.assemble(BasicResultAssembler.java:58) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.extractConcreteTypeStateValues(EntityInitializerImpl.java:1635) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeEntityInstance(EntityInitializerImpl.java:1353) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeInstance(EntityInitializerImpl.java:1331) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.graph.entity.internal.EntityInitializerImpl.initializeInstance(EntityInitializerImpl.java:95) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.StandardRowReader.coordinateInitializers(StandardRowReader.java:239) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.internal.StandardRowReader.readRow(StandardRowReader.java:136) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.readUniqueAssert(ListResultsConsumer.java:258) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.readRows(ListResultsConsumer.java:232) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:166) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.results.spi.ListResultsConsumer.consume(ListResultsConsumer.java:33) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.doExecuteQuery(JdbcSelectExecutorStandardImpl.java:201) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.internal.JdbcSelectExecutorStandardImpl.executeQuery(JdbcSelectExecutorStandardImpl.java:100) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.executeQuery(JdbcSelectExecutor.java:64) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.sql.exec.spi.JdbcSelectExecutor.list(JdbcSelectExecutor.java:138) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.ast.internal.SingleIdLoadPlan.load(SingleIdLoadPlan.java:143) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.ast.internal.SingleIdLoadPlan.load(SingleIdLoadPlan.java:115) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.ast.internal.SingleIdEntityLoaderStandardImpl.load(SingleIdEntityLoaderStandardImpl.java:66) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.persister.entity.AbstractEntityPersister.doLoad(AbstractEntityPersister.java:3529) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.persister.entity.AbstractEntityPersister.load(AbstractEntityPersister.java:3518) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.loadFromDatasource(DefaultLoadEventListener.java:596) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.loadFromCacheOrDatasource(DefaultLoadEventListener.java:570) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.load(DefaultLoadEventListener.java:554) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.doLoad(DefaultLoadEventListener.java:538) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.load(DefaultLoadEventListener.java:204) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.loadWithRegularProxy(DefaultLoadEventListener.java:284) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.proxyOrLoad(DefaultLoadEventListener.java:236) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.doOnLoad(DefaultLoadEventListener.java:109) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.internal.DefaultLoadEventListener.onLoad(DefaultLoadEventListener.java:68) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.event.service.internal.EventListenerGroupImpl.fireEventOnEachListener(EventListenerGroupImpl.java:151) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.internal.SessionImpl.fireLoadNoChecks(SessionImpl.java:1291) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.internal.SessionImpl.fireLoad(SessionImpl.java:1278) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]     
        at org.hibernate.internal.SessionImpl.load(SessionImpl.java:1260) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.doLoad(IdentifierLoadAccessImpl.java:181) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.lambda$load$1(IdentifierLoadAccessImpl.java:167) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.perform(IdentifierLoadAccessImpl.java:134) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.loader.internal.IdentifierLoadAccessImpl.load(IdentifierLoadAccessImpl.java:167) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.internal.SessionImpl.find(SessionImpl.java:2407) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at org.hibernate.internal.SessionImpl.find(SessionImpl.java:2381) ~[hibernate-core-7.1.8.Final.jar:7.1.8.Final]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.orm.jpa.ExtendedEntityManagerCreator$ExtendedEntityManagerInvocationHandler.invoke(ExtendedEntityManagerCreator.java:362) ~[spring-orm-7.0.1.jar:7.0.1]
        at jdk.proxy2/jdk.proxy2.$Proxy146.find(Unknown Source) ~[na:na]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.orm.jpa.SharedEntityManagerCreator$SharedEntityManagerInvocationHandler.invoke(SharedEntityManagerCreator.java:317) ~[spring-orm-7.0.1.jar:7.0.1]
        at jdk.proxy2/jdk.proxy2.$Proxy146.find(Unknown Source) ~[na:na]
        at org.springframework.data.jpa.repository.support.SimpleJpaRepository.findById(SimpleJpaRepository.java:344) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.aop.support.AopUtils.invokeJoinpointUsingReflection(AopUtils.java:359) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker$RepositoryFragmentMethodInvoker.lambda$new$0(RepositoryMethodInvoker.java:278) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.doInvoke(RepositoryMethodInvoker.java:169) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryMethodInvoker.invoke(RepositoryMethodInvoker.java:158) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryComposition$RepositoryFragments.invoke(RepositoryComposition.java:545) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryComposition.invoke(RepositoryComposition.java:290) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.RepositoryFactorySupport$ImplementationMethodExecutionInterceptor.invoke(RepositoryFactorySupport.java:708) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.doInvoke(QueryExecutorMethodInterceptor.java:171) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.data.repository.core.support.QueryExecutorMethodInterceptor.invoke(QueryExecutorMethodInterceptor.java:146) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.projection.DefaultMethodInvokingMethodInterceptor.invoke(DefaultMethodInvokingMethodInterceptor.java:69) ~[spring-data-commons-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.transaction.interceptor.TransactionAspectSupport.invokeWithinTransaction(TransactionAspectSupport.java:370) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.transaction.interceptor.TransactionInterceptor.invoke(TransactionInterceptor.java:118) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.dao.support.PersistenceExceptionTranslationInterceptor.invoke(PersistenceExceptionTranslationInterceptor.java:135) ~[spring-tx-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.data.jpa.repository.support.CrudMethodMetadataPostProcessor$CrudMethodMetadataPopulatingMethodInterceptor.invoke(CrudMethodMetadataPostProcessor.java:166) ~[spring-data-jpa-4.0.0.jar:4.0.0]
        at org.springframework.aop.framework.ReflectiveMethodInvocation.proceed(ReflectiveMethodInvocation.java:179) ~[spring-aop-7.0.1.jar:7.0.1]
        at org.springframework.aop.framework.JdkDynamicAopProxy.invoke(JdkDynamicAopProxy.java:222) ~[spring-aop-7.0.1.jar:7.0.1]
        at jdk.proxy2/jdk.proxy2.$Proxy159.findById(Unknown Source) ~[na:na]
        at com.hotel.hotel.controller.BookingController.cancelBooking(BookingController.java:190) ~[classes/:na]
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103) ~[na:na]       
        at java.base/java.lang.reflect.Method.invoke(Method.java:580) ~[na:na]
        at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:258) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:190) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:934) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:853) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:86) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1003) ~[spring-webmvc-7.0.1.jar:7.0.1]
        at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:877) ~[spring-webmvc-7.0.1.jar:7.0.1] 
        at jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710) ~[tomcat-embed-core-11.0.14.jar:6.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:128) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-11.0.14.jar:11.0.14] 
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199) ~[spring-web-7.0.1.jar:7.0.1]
        at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116) ~[spring-web-7.0.1.jar:7.0.1]
        at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:107) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:482) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:113) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:83) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:72) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:341) ~[tomcat-embed-core-11.0.14.jar:11.0.14] 
        at org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:397) ~[tomcat-embed-core-11.0.14.jar:11.0.14]  
        at org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:63) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:903) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:1778) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:52) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:946) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:480) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:57) ~[tomcat-embed-core-11.0.14.jar:11.0.14]
        at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
