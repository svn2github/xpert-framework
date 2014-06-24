<ui:composition  xmlns="http://www.w3.org/1999/xhtml"
                 xmlns:h="http://java.sun.com/jsf/html"
                 xmlns:f="http://java.sun.com/jsf/core"
                 xmlns:ui="http://java.sun.com/jsf/facelets"
                 xmlns:p="http://primefaces.org/ui"
                 xmlns:x="http://xpert.com/faces">
    
  
    <h:form id="formCreate${entity.name}">
        <p:fieldset legend="${sharp}{xmsg['generalData']}">
            <x:modalMessages/>
            <h:panelGrid columns="2" styleClass="grid-form">
                 <#list entity.fields as field>
                 <#if field.id == false>

                     <h:outputLabel value="<#if field.required == true>* </#if>${sharp}{${resourceBundle}['${entity.nameLower}.${field.name}']}:" />
                     <#-- String -->
                     <#if field.string == true>
                     <p:inputText value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" maxlength="${field.maxlength?string}"  size="70"  />
                     </#if>
                     <#-- Boolean -->
                     <#if field.yesNo == true>
                     <h:selectBooleanCheckbox value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" />
                     </#if>
                     <#-- Integer/Long -->
                     <#if field.integer == true>
                     <p:inputMask mask="9?999999999" placeHolder="" value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}"  />
                     </#if>
                     <#-- Decimal (BigDecimal, Double) -->
                     <#if field.decimal == true>
                     <x:inputNumber value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" />
                     </#if>
                     <#-- Date -->
                     <#if field.date == true>
                     <p:calendar value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" />
                     </#if>
                     <#-- Enuns/ManyToOne (render a combobox) -->
                     <#if field.enumeration == true || field.manyToOne == true>
                     <h:selectOneMenu value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" <#if field.enumeration == false>converter="entityConverter"</#if>  >
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
                     <h:selectManyCheckbox value="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity.${field.name}}" converter="entityConverter" >
                         <#if field.lazy == true>
                         <x:initializer/>
                         </#if>
                         <f:selectItems value="${sharp}{findAllBean.get(class${configuration.managedBeanSuffix}.${field.typeNameLower})}" 
                                        var="${field.typeNameLower}"
                                        itemLabel="${sharp}{${field.typeNameLower}}"/>
                     </h:selectManyCheckbox>
                     </#if>
                 </#if>
                 </#list>

                </h:panelGrid>
          </p:fieldset>
          <h:outputText value="${sharp}{xmsg['requiredFieldsForm']}"/>
          <div class="uix-center">
              <x:securityArea rolesAllowed="${entity.nameLower}.create">
                   <p:commandButton process="@form" update="@form" action="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.save}" value="${sharp}{xmsg['save']}" />
              </x:securityArea>
              <x:securityArea rolesAllowed="${entity.nameLower}.audit">
                   <x:audit for="${sharp}{${entity.nameLower}${configuration.managedBeanSuffix}.entity}"/>
              </x:securityArea>
          </div>
       </h:form>

</ui:composition>