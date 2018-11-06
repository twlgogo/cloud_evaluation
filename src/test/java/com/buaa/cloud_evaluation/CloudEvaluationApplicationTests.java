package com.buaa.cloud_evaluation;

import com.buaa.cloud_evaluation.mapper.IndexMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CloudEvaluationApplicationTests {
    @Autowired
    private IndexMapper indexMapper;

    @Test
    public void test() {
        indexMapper.insertByNameAndParent("root",1);
    }

}
