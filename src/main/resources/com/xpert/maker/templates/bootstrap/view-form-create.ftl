<ui:composition  xmlns="http://www.w3.org/1999/xhtml"
                 xmlns:h="http://java.sun.com/jsf/html"
                 xmlns:f="http://java.sun.com/jsf/core"
                 xmlns:ui="http://java.sun.com/jsf/facelets"
                 xmlns:p="http://primefaces.org/ui"
                 xmlns:x="http://xpert.com/faces">
    
  
    <h:form id="formCreate${entity.name}" styleClass="form-detail">
        <p:fieldset legend="${sharp}{xmsg['generalData']}">
            <x:modalMessages/>

               <div class="container-fluid">
                    <div class="row">
                    <#list entity.fields as field>
                    <#if field.renderFieldInFormCreate == true >
                         <div class="form-group ${configuration.bootstrapVersion.defaultColumns}">
                            <#-- Boolean -->
                            <#if field.yesNo == true>
                            <div class="checkbox">
                                <label>
                                    <h:selectBooleanCheckbox id="${field.name}" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" />
                                    ${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}
                                </label>
                            </div>
                            </#if>
                            <#-- Others -->
                            <#if field.yesNo == false>
                            <#-- String -->
                            <#if field.string == true>
                            <h:outputLabel for="${field.name}" value="<#if field.required == true>* </#if>${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}:" />
                            <p:inputText id="${field.name}" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" maxlength="${field.maxlength?string}"  styleClass="form-control"  />
                            </#if>
                            <#-- Integer/Long -->
                            <#if field.integer == true>
                            <h:outputLabel for="${field.name}" value="<#if field.required == true>* </#if>${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}:" />
                            <p:inputMask id="${field.name}" mask="9?999999999" placeHolder="" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" styleClass="form-control" />
                            </#if>
                            <#-- Decimal (BigDecimal, Double) -->
                            <#if field.decimal == true>
                            <h:outputLabel for="${field.name}:input" value="<#if field.required == true>* </#if>${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}:" />
                            <x:inputNumber id="${field.name}" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" styleClass="form-control"/>
                            </#if>
                            <#-- Date p:calendar wont work with form-control, he has a span over the input and class goes for span -->
                            <#if field.date == true>
                            <h:outputLabel for="${field.name}" value="<#if field.required == true>* </#if>${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}:" />
                            <#if configuration.maskCalendar == true>
                            <p:calendar id="${field.name}" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" styleClass="uix-calendar" showOn="button" pattern="${configuration.datePattern}" >
                                <x:mask>
                            </p:calendar>
                            <#else>
                            <p:calendar id="${field.name}" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" styleClass="uix-calendar" showOn="button" pattern="${configuration.datePattern}" />
                            </#if>
                            </#if>
                            <#-- Enuns/ManyToOne (render a combobox) -->
                            <#if field.enumeration == true || field.manyToOne == true>
                            <h:outputLabel for="${field.name}" value="<#if field.required == true>* </#if>${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}:" />
                            <h:selectOneMenu id="${field.name}" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" <#if field.enumeration == false>converter="entityConverter"</#if> styleClass="form-control" >
                                <#if field.lazy == true>
                                <x:initializer/>
                                </#if>
                                <f:selectItem itemLabel="${sharp}{xmsg['select']}" />
                                <f:selectItems value="${sharp}{findAllBean.get(class${configuration.managedBeanSuffix}.${field.typeNameLower})}" 
                                               var="${field.typeNameLower}"
                                               itemLabel="${sharp}{${field.typeNameLower}}"/>
                            </h:selectOneMenu>
                            </#if>
                            <#-- Collections (render a checkbox list) -->
                            <#if field.manyToMany == true >
                            <h:outputLabel value="<#if field.required == true>* </#if>${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}:" />
                            <h:selectManyCheckbox id="${field.name}" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" converter="entityConverter" >
                                <#if field.lazy == true>
                                <x:initializer/>
                                </#if>
                                <f:selectItems value="${sharp}{findAllBean.get(class${configuration.managedBeanSuffix}.${field.typeNameLower})}" 
                                               var="${field.typeNameLower}"
                                               itemLabel="${sharp}{${field.typeNameLower}}"/>
                            </h:selectManyCheckbox>
                            </#if>
                            </#if>
                        </div>
                    </#if>
                    </#list>

                    </div>
                </div>
          </p:fieldset>
          <h:outputText value="${sharp}{xmsg['requiredFieldsForm']}"/>
          <div class="uix-center">
              <#if configuration.generatesSecurityArea == true >
              <x:securityArea rolesAllowed="${entity.nameLower}.create">
                   <p:commandButton process="@form" update="@form" action="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.save}" value="${sharp}{xmsg['save']}" />
              </x:securityArea>
              <x:securityArea rolesAllowed="${entity.nameLower}.audit">
                   <x:audit for="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity}"/>
              </x:securityArea>
              </#if>
              <#if configuration.generatesSecurityArea == false >
              <p:commandButton process="@form" update="@form" action="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.save}" value="${sharp}{xmsg['save']}" />
              <x:audit for="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity}"/>
              </#if>
          </div>
       </h:form>

</ui:composition>