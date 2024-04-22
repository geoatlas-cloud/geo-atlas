package org.geoatlas.pyramid.action.vector;

import org.geoatlas.pyramid.action.ActionChain;
import org.geoatlas.pyramid.action.ActionContext;
import org.geoatlas.pyramid.action.ActionStep;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:43
 * @since: 1.0
 **/
public class FilterActionStep implements ActionStep {
    @Override
    public void doAction(ActionContext context, ActionChain chain) throws IOException, SQLException {
        // 基于内存的数据选取动作, 比如根据密度 面积 周长等因素处理
    }
}
