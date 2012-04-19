/*
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */

Ext.ns("Sonia.notify");

Sonia.notify.ConfigPanel = Ext.extend(Sonia.repository.PropertiesFormPanel, {
  
  titleText: 'Notification',
  
  notifyRepositoryContactText: 'Notify Repository Contact',
  
  // TODO
  notifyRepositoryContactHelpText: '',
  contactGridHelpText: '',
  
  contactStore: null,
  
  initComponent: function(){
    this.contactStore = new Ext.data.ArrayStore({
      fields: [
        {name: 'contact'}
      ],
      sortInfo: {
        field: 'contact'
      }
    });
    
    this.loadContacts(this.contactStore, this.item);
    
    var contactColModel = new Ext.grid.ColumnModel({
      defaults: {
        sortable: true,
        editable: true
      },
      columns: [{
        id: 'contact',
        dataIndex: 'contact',
        header: this.colNameText,
        editor: Ext.form.TextField
      }]
    });
    
    var selectionModel = new Ext.grid.RowSelectionModel({
      singleSelect: true
    });
    
    var config = {
      title: this.titleText,
      items: [{
        xtype: 'checkbox',
        name: 'notify-contact-repository',
        property: 'notify.contact.repository',
        inputValue: 'true',
        fieldLabel : this.notifyRepositoryContactText,
        helpText: this.notifyRepositoryContactHelpText  
      },{
        id: 'contactGrid',
        xtype: 'editorgrid',
        clicksToEdit: 1,
        autoExpandColumn: 'uri',
        frame: true,
        width: '100%',
        autoHeight: true,
        autoScroll: false,
        colModel: contactColModel,
        sm: selectionModel,
        store: this.contactStore,
        viewConfig: {
          forceFit:true
        },
        tbar: [{
          text: this.addText,
          scope: this,
          icon: this.addIcon,
          handler : function(){
            var Contact = this.contactStore.recordType;
            var p = new Contact();
            var grid = Ext.getCmp('contactGrid');
            grid.stopEditing();
            this.contactStore.insert(0, p);
            grid.startEditing(0, 0);
          }
        },{
          text: this.removeText,
          scope: this,
          icon: this.removeIcon,
          handler: function(){
            var grid = Ext.getCmp('contactGrid');
            var selected = grid.getSelectionModel().getSelected();
            if ( selected ){
              this.contactStore.remove(selected);
            }
          }
        }, '->',{
          id: 'contactGridHelp',
          xtype: 'box',
          autoEl: {
            tag: 'img',
            src: 'resources/images/help.gif'
          }
        }]

      }]
    };
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.notify.ConfigPanel.superclass.initComponent.apply(this, arguments);
  },
  
  afterRender: function(){
    // call super
    Sonia.notify.ConfigPanel.superclass.afterRender.apply(this, arguments);

    Ext.QuickTips.register({
      target: Ext.getCmp('contactGridHelp'),
      title: '',
      text: this.contactGridHelpText,
      enabled: true
    });
  },
  
  loadContacts: function(store, repository){
    if (debug){
      console.debug('load contacts property');
    }
    if (!repository.properties){
      repository.properties = [];
    }
    Ext.each(repository.properties, function(prop){
      if ( prop.key == 'notify.contact.list' ){
        var value = prop.value;
        this.parseContacts(store, value);
      }
    }, this);
  },
  
  parseContacts: function(store, contactsString){
    var contactArray = contactsString.split(';');
    Ext.each(contactArray, function(contactString){
      if (contactString.length > 0){
        var Contact = store.recordType;
        var c = new Contact({contact: contactString});
        store.add(c);
      }
    });
  },
  
  storeExtraProperties: function(repository){
    if (debug){
      console.debug('store contact properties');
    }
    
    // delete old contacts
    Ext.each(repository.properties, function(prop, index){
      if ( prop.key == 'notify.contact.list' ){
        delete repository.properties[index];
      }
    });
    
    var contactsString = '';
    this.contactStore.data.each(function(r){
      var contactData = r.data;
      contactsString += contactData.contact + ';';
    });
    
    if (debug){
      console.debug('add contact string: ' + contactsString);
    }
    
    repository.properties.push({
      key: 'notify.contact.list',
      value: contactsString
    });
  }
  
});

Ext.reg("notifyConfigPanel", Sonia.notify.ConfigPanel);