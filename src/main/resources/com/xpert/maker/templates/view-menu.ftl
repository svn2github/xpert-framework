<ui:composition  xmlns="http://www.w3.org/1999/xhtml"
                 xmlns:h="http://java.sun.com/jsf/html"
                 xmlns:f="http://java.sun.com/jsf/core"
                 xmlns:ui="http://java.sun.com/jsf/facelets"
                 xmlns:p="http://primefaces.org/ui"
                 xmlns:x="http://xpert.com/faces">
    <p:toolbar>
        <p:toolbarGroup align="left">  
            <x:securityArea rolesAllowed="${entity.nameLower}.list">
                <p:button icon="ui-icon-search" value="${sharp}{xmsg['list']}" outcome="list${entity.name}" />
            </x:securityArea>
            <x:securityArea rolesAllowed="${entity.nameLower}.create">
                <p:button icon="ui-icon-plus" value="${sharp}{xmsg['create']}" outcome="create${entity.name}" />
            </x:securityArea>
        </p:toolbarGroup>
    </p:toolbar>
</ui:composition>