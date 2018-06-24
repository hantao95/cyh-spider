package cn.zero.spider.webmagic.page;

import cn.zero.spider.pojo.Article;
import cn.zero.spider.pojo.Book;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.utils.UrlUtils;

/**
 * 笔趣阁爬虫
 * http://www.biquge.com.tw/
 *
 *
 * @author 蔡元豪
 * @date 2018/6/23 15:57
 */
@Component
public class BiQuGePageProcessor implements PageProcessor {

    private Logger logger = LoggerFactory.getLogger(BiQuGePageProcessor.class);


    private Site site = Site.me()
            //下载失败的url重新放入队列尾部重试
            .setCycleRetryTimes(3)
            //重试次数
            .setRetryTimes(3)
            //2页处理间隔 单位毫秒
            .setSleepTime(100)
            //超时时间
            .setTimeOut(3000)
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36");

    @Override
    public void process(Page page) {
        String siteUrl = UrlUtils.getHost(page.getUrl().toString()) + "/";
        logger.info("爬取地址：" + siteUrl);
        if (page.getUrl().regex(siteUrl + "\\w+/\\w+.html").toString() != null) {
            //获取单个章节
            getArticle(page);
        } else {
            //获取小说详情
            getBook(page);
        }

    }

    public void getArticle(Page page) {
        Article article = new Article();
        //设置章节目录
        article.setUrl(Integer.parseInt(page.getUrl().regex("http://www.biquge.com.tw/\\w+/(\\w+)").toString()));
        //设置小说目录
        article.setBookUrl(page.getUrl().regex("http://www.biquge.com.tw/(\\w+)").toString());
        //章节名
        article.setTitle(page.getHtml().css("#wrapper > div.content_read > div > div.bookname > h1").xpath("//*/text()").toString());
        //章节正文
        article.setContent(page.getHtml().xpath("//*[@id=\"content\"]/html()").toString());
        if (article.getTitle() == null
                || StringUtils.isBlank(article.getTitle())) {
            page.setSkip(true);
            logger.info("爬取小说章节失败："+ page.getUrl().toString());
        }else {
            page.putField("article",article);
            logger.info("爬取小说章节:" + article.getTitle() + "----成功--->"+page.getUrl().toString());
        }
    }

    /**
     * 小说详情
     *
     * @param page
     */
    public void getBook(Page page) {
        String siteUrl = UrlUtils.getHost(page.getUrl().toString()) + "/";
        logger.info("开始爬取小说详情：" + page.getUrl());
        Book book = new Book();
        book.setBookUrl(page.getUrl().regex("(\\w+)").toString());
        book.setAuthor(page.getHtml().xpath("//*[@id=\"info\"]/p[1]/text()").toString());
        book.setTitle(page.getHtml().xpath("//*[@id=\"info\"]/h1/text()").toString());
        book.setUpdateTime(page.getHtml().xpath("//*[@id=\"info\"]/p[3]/text()").toString());
        book.setIntro(page.getHtml().xpath("//*[@id=\"intro\"]/p/text()").toString());
        book.setLatestChapterTitle(page.getHtml().xpath("//*[@id=\"info\"]/p[4]/a/text()").toString());
        book.setLatestChapterUrl(page.getHtml().xpath("//*[@id=\"info\"]/p[4]/a/@href").regex("(\\w+)\\.html").toString());
        book.setTitlePageUrl(page.getHtml().xpath("//*[@id=\"fmimg\"]/img/@src").toString());
        book.setSourceUrl(siteUrl);
        book.setChapterPage(page.getHtml().xpath("//*[@id=\"list\"]/dl")
                //取消域名 只保存相对地址
                .replace(siteUrl, "")
                .get());
        page.addTargetRequests(page.getHtml().xpath("//*[@id=\"list\"]/dl").links().all());
        if (book.getTitle() == null
                || StringUtils.isBlank(book.getTitle())) {
            page.setSkip(true);
        }
        page.putField("book", book);
        logger.info("爬取《" + book.getTitle() + "》小说详情成功：" + page.getUrl());
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        Spider.create(new BiQuGePageProcessor())
                .addUrl("http://www.biquge.com.tw/16_16209/")
                .thread(5).run();
    }
}
