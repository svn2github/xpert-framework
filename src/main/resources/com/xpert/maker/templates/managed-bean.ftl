package ${configuration.managedBean};


import java.io.Serializable;
import com.xpert.core.crud.AbstractBaseBean;
import com.xpert.core.crud.AbstractBusinessObject;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import ${configuration.businessObject}.${name}${configuration.businessObjectSuffix};
import ${entity.name};

/**
 *
 * @author ${author}
 */
@ManagedBean
@ViewScoped
public class ${name}${configuration.managedBeanSuffix} extends AbstractBaseBean<${name}> implements Serializable {

    @EJB
    private ${name}${configuration.businessObjectSuffix} ${nameLower}${configuration.businessObjectSuffix};

    @Override
    public AbstractBusinessObject getBO() {
        return ${nameLower}${configuration.businessObjectSuffix};
    }

    @Override
    public String getDataModelOrder() {
        return "id";
    }
}
