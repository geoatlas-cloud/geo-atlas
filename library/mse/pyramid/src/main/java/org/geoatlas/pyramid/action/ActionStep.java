package org.geoatlas.pyramid.action;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author: <a href="mailto:thread.zhou@gmail.com">Fuyi</a>
 * @time: 2024/4/21 16:39
 * @since: 1.0
 **/
public interface ActionStep extends Priority{

    void doAction(ActionContext context, ActionChain chain) throws IOException, SQLException;
}
