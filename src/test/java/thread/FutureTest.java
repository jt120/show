package thread;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.*;
import org.junit.After;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.*;

/**
 * Created by ze.liu on 2014/10/17.
 */
public class FutureTest {
    ExecutorService executorService = Executors.newCachedThreadPool();
    @Test
    public void test01() {
        Future<Object> submit = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Thread.sleep(2000);
                return "hello";
            }
        });
        System.out.println("wait...");
        try {
            Object o = submit.get(2000, TimeUnit.SECONDS);
            System.out.println(o);
            //需要catch一堆异常
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test02() {
        final ListenableFuture<Object> listenableFuture = query("zhang");
        System.out.println("start");
        //runnable的监听器
        listenableFuture.addListener(new Runnable() {
            @Override
            public void run() {
                Object o = null;
                try {
                    Object uninterruptibly = Uninterruptibles.getUninterruptibly(listenableFuture);
                    System.out.println(uninterruptibly);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                System.out.println(o);
            }
        }, executorService);
        System.out.println("end");
    }


    private ListenableFuture<Object> query(final String name) {
        ListeningExecutorService service = MoreExecutors.listeningDecorator(executorService);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return service.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return "hello " + name;
            }
        });
    }


    @Test
    public void test03() {
        ListenableFuture<Object> listenableFuture = query("lisi");
        //使用callback
        Futures.addCallback(listenableFuture, new FutureCallback<Object>() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("success " + result);
            }

            @Override
            public void onFailure(Throwable t) {
                System.out.println("fail");
            }
        });
        System.out.println("start");
    }


    private List<ListenableFuture<Object>> queryMore(final String[] name) {
        List<ListenableFuture<Object>> listenableFutures = Lists.newArrayListWithCapacity(name.length);
        ListeningExecutorService service = MoreExecutors.listeningDecorator(executorService);
        for (int i = 0; i < name.length; i++) {
            final int j = i;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           listenableFutures.add(service.submit(new Callable<Object>() {
               @Override
               public Object call() throws Exception {
                   String s = "hello " + j + " " + name[j];
                   //System.out.println(s);
                   return s;
               }
           }));
        }
        return listenableFutures;

    }

    @Test
    public void test04() {
        final List<ListenableFuture<Object>> listenableFutures = queryMore(new String[]{"zhang", "li", "qian"});
        final List<Object> list = Lists.newArrayListWithCapacity(listenableFutures.size());
        //这种方法还没取到值，所以是空啊！
        //for (int i = 0; i < list.size(); i++) {
        //    final int j = i;
        //    try {
        //        System.out.println(listenableFutures.get(i).get());
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    } catch (ExecutionException e) {
        //        e.printStackTrace();
        //    }
        //    Futures.addCallback(listenableFutures.get(i),new FutureCallback<Object>() {
        //        @Override
        //        public void onSuccess(Object result) {
        //            System.out.println(result);
        //            list.add(result);
        //        }
        //
        //        @Override
        //        public void onFailure(Throwable t) {
        //            list.add("fail " + j);
        //        }
        //    });
        //}
        for (ListenableFuture<Object> listenableFuture : listenableFutures) {
            Futures.addCallback(listenableFuture,new FutureCallback<Object>() {
                @Override
                public void onSuccess(Object result) {
                    list.add(result);
                }

                @Override
                public void onFailure(Throwable t) {

                }
            });
        }
        System.out.println(list);
    }

    @Test
    public void test5() {
        List<ListenableFuture<Object>> listenableFutures = queryMore(new String[]{"zhang", "li", "qian"});
// succeeds, with null in place of failures
        ListenableFuture<List<Object>> listenableFutures1 = Futures.successfulAsList(listenableFutures);

        Futures.addCallback(listenableFutures1, new FutureCallback<List<Object>>() {
            @Override
            public void onSuccess(List<Object> result) {
                System.out.println(result);
            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
    }

    @After
    public void destroy() {
        executorService.shutdown();
    }
}
