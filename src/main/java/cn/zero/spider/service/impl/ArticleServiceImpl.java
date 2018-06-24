package cn.zero.spider.service.impl;

import cn.zero.spider.dao.ArticleMapper;
import cn.zero.spider.pojo.Article;
import cn.zero.spider.service.IArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 蔡元豪
 * @date 2018/6/23 22:35
 */
@Service
public class ArticleServiceImpl extends BaseServiceImpl<Article> implements IArticleService {


    private ArticleMapper articleMapper;

    @Autowired
    public void setArticleMapper(ArticleMapper articleMapper) {
        this.articleMapper = articleMapper;
    }

    @Override
    public Article getByUrl(String bookUrl, String articleUrl) {
        return articleMapper.getByUrl(bookUrl, articleUrl);
    }
}
