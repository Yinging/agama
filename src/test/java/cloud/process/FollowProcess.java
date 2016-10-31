package cloud.process;

import cloud.Constant;
import cloud.RequestUtil;
import cloud.Singleton;
import cloud.entity.YunUser;
import com.github.gin.agama.core.CrawlConfiger;
import com.github.gin.agama.core.JCrawler;
import com.github.gin.agama.processer.PageProcess;
import com.github.gin.agama.proxy.HttpProxy;
import com.github.gin.agama.proxy.ProxyPool;
import com.github.gin.agama.site.Page;
import com.github.gin.agama.site.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Proxy;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Created by FSTMP on 2016/10/27.
 */
public class FollowProcess implements PageProcess {
    Logger logger = LoggerFactory.getLogger(FollowProcess.class);

    @Override
    public void process(Page page) {
        //设置用户的订阅者是否获取完毕
        Matcher m = Constant.USER_PATTERN.matcher(page.getUrl());
        if(m.find()){
            String uk = m.group(1);
            Singleton.getYunUserService().updateFollowCrawled(Long.parseLong(uk));
        }

        //为了follow和YunUser可以重用
        page.setRawText(page.getRawText().replace("follow_list", "list").replace("follow_uname", "uname").replace("follow_uk", "uk"));
        if(logger.isDebugEnabled())
            logger.debug("follow:"+page.getRawText());
        List<YunUser> userList = page.getRender().renderToJson().toEntityList(YunUser.class);

        //循环获取订阅者的链接
        for(YunUser user : userList){
            for(int i =0 ; i < user.getFollowCount();i = i+ Constant.LIMIT){
                Request request = RequestUtil.createRequest();
                request.setUrl(String.format(Constant.FOLLOW_URL, user.getUk(), Constant.LIMIT, i));
                page.getRequests().add(request);
            }
        }
        //保存订阅者
        page.getResultItems().add(userList);
    }

}