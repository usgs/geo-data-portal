/**
 * Ext.ux.Notify
 * @version 1.0
 * Copyright(c) 2009 nXgen Web Solutions
 * http://www.nxgenwebsolutions.com
 *  
 * ---------------------------------------------------------------------------
 * 
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see http://www.gnu.org/licenses/.
 * 
 * ---------------------------------------------------------------------------
 * 
 * This extension is based on the work of efattal from the ExtJS forums.
 * http://extjs.com/forum/showthread.php?t=32365
 * 
 * This extension provides an easy and simple notification system for web
 * applications. It supports a notification title and message with optional
 * icons associated with each. Please read through the configuration options 
 * in Ext.ux.NotifyMgr and Ext.ux.Notify to learn how to work with this extension.
 * 
 * To create a new notification window, use the following code:
 * 
 *	 new Ext.ux.Notify({
 * 			msgWidth: 200,
 *			hideDelay: 2000,
 * 			title: 'Hi',
 *			msg: 'Hello, world!'
 *		}).show(document);
 *
 * The above code will create a notification window that is 200px wide and
 * fades out in 2 seconds.
 * 
 * Please note: You must set 'overflow:hidden' to the body element if you are
 * bottom aligning the notifications to prevent scrollbars from being created.
 * 
 */
// create namespaces for the notify extension
Ext.namespace('Ext.ux.Notify');
Ext.namespace('Ext.ux.NotifyMgr');
/**
 * @object Ext.ux.NotifyMgr
 * This objects stores configuration options and temporary data for the notifications.
 */
Ext.ux.NotifyMgr = {
	/**
     * @cfg {String} msgIconBaseCls
     * The base class to apply to the message icon. By default, a base class will not
     * be applied.
     */
	msgIconBaseCls: 'msgicon',
	/**
     * @cfg {String} msgStyle
     * Custom CSS styles to be applied to the notification message body in the format
     * expected by {@link Ext.Element#applyStyles}.
     */
	msgStyle: 'padding:10px',
	/**
     * @cfg {String} alignment
     * The alignment of the notification window.
     * 
     * Possible values:
     * 		top-left
     * 		top-center
     * 		top-right
     * 		bottom-left
     * 		bottom-center
     * 		bottom-right (default)
     */
	alignment: 'bottom-right',
	/**
     * @cfg {Array} offsets
     * An array (2 numbers) of offsets to be applied notifications after alignment.
     * The first number is the offset from the top/bottom. The second is the offset from
     * the left/right (does not apply to center aligned notifications). Defaults to [10, 10].
     */
	offsets: [10, 10],
	/**
     * @cfg {Number} spacing
     * The height in pixels of the space between each notification window. Defaults to 10.
     */
	spacing: 10,
	/**
     * @cfg {Number} zseed
     * The z-index to apply to notification windows.
     */
	zIndex: 30000,
	// internal use only, do not modify
    positions: [],
	// internal use only, do not modify
	windows: []
};
/**
 * Monitor window size and adjust notification window positions, if necessary.
 */
Ext.EventManager.on(window, 'resize', function(){
	var b = Ext.getBody(),
		bw = b.getComputedWidth(),
		bh = b.getComputedHeight();
	if (!Ext.ux.NotifyMgr.windows.length) return;
	for (var i = 0; i < Ext.ux.NotifyMgr.windows.length; ++i) {
		var w = Ext.ux.NotifyMgr.windows[i].getInnerWidth() + Ext.ux.NotifyMgr.windows[i].getFrameWidth(),
			h = Ext.ux.NotifyMgr.windows[i].getInnerHeight() + Ext.ux.NotifyMgr.windows[i].getFrameHeight();
		switch (Ext.ux.NotifyMgr.alignment) {
			case 'top-center':
				Ext.ux.NotifyMgr.windows[i].el.setX(bw - parseInt(bw / 2) - parseInt(w / 2));
				break;
			case 'top-right':
				Ext.ux.NotifyMgr.windows[i].el.setX(bw - w - (Ext.ux.NotifyMgr.offsets[1] || 10));
				break;
			case 'bottom-left':
				var offset = 0;
				for (var j = i + 1; j < Ext.ux.NotifyMgr.windows.length; ++j) {
					offset += Ext.ux.NotifyMgr.windows[j].getInnerHeight() + Ext.ux.NotifyMgr.windows[j].getFrameHeight() + (Ext.ux.NotifyMgr.spacing || 10);
				}
				Ext.ux.NotifyMgr.windows[i].el.setY(bh - h - offset - (Ext.ux.NotifyMgr.offsets[0] || 10));
				break;
			case 'bottom-center':
				var offset = 0;
				for (var j = i + 1; j < Ext.ux.NotifyMgr.windows.length; ++j) {
					offset += Ext.ux.NotifyMgr.windows[j].getInnerHeight() + Ext.ux.NotifyMgr.windows[j].getFrameHeight() + (Ext.ux.NotifyMgr.spacing || 10);
				}
				Ext.ux.NotifyMgr.windows[i].el.setY(bh - h - offset - (Ext.ux.NotifyMgr.offsets[0] || 10));
				Ext.ux.NotifyMgr.windows[i].el.setX(bw - parseInt(bw / 2) - parseInt(w / 2));
				break;
			case 'bottom-right':
				var offset = 0;
				for (var j = i + 1; j < Ext.ux.NotifyMgr.windows.length; ++j) {
					offset += Ext.ux.NotifyMgr.windows[j].getInnerHeight() + Ext.ux.NotifyMgr.windows[j].getFrameHeight() + (Ext.ux.NotifyMgr.spacing || 10);
				}
				Ext.ux.NotifyMgr.windows[i].el.setY(bh - h - offset - (Ext.ux.NotifyMgr.offsets[0] || 10));
				Ext.ux.NotifyMgr.windows[i].el.setX(bw - w - (Ext.ux.NotifyMgr.offsets[1] || 10));
				break;
		}
	}
});
/**
 * @class Ext.ux.Notify
 * @extends Ext.Window
 * This class defines a notification.
 */
Ext.ux.Notify = Ext.extend(Ext.Window, {
	/**
     * @cfg {Number} msgWidth
     * The width in pixels of the notification window. This value defaults to 250.
     */
	/**
     * @cfg {String} titleIconCls
     * The CSS class for the notification title icon. This is equivalent to the iconCls
     * option of a normal {@link Ext.Window}.
     */
	/**
     * @cfg {String} msgIconCls
     * The CSS class for the notification message icon.
     */
	initComponent: function(){
		Ext.apply(this, {
			shadow:false,
			draggable:false,
			closable: this.isClosable || false,
			resizable:false,
			iconCls: this.titleIconCls || '',
			msgIconCls: this.msgIconCls || '',
			buttonAlign: 'center',
			bodyStyle: Ext.ux.NotifyMgr.msgStyle
		});
		this.task = new Ext.util.DelayedTask(this.hide, this);
		if (this.msgIconCls && this.msgIconCls !== '') {
			var baseCls = Ext.ux.NotifyMgr.msgIconBaseCls !== '' ? Ext.ux.NotifyMgr.msgIconBaseCls + ' ' : '';
			this.html = '<div class="'+baseCls+this.msgIconCls+'"></div>' + this.msg;
		} else {
			this.html = this.msg;
		}
		Ext.ux.Notify.superclass.initComponent.call(this);
	},
	onDestroy: function(){
		Ext.ux.NotifyMgr.positions.remove(this.pos);
		Ext.ux.NotifyMgr.windows.remove(this);
		Ext.ux.Notify.superclass.onDestroy.call(this);
	},
	afterShow: function(){
		if(Ext.isMac && Ext.isGecko){ // work around stupid FF 2.0/Mac scroll bar bug
        	this.cascade(this.setAutoScroll);
        }
		if(this.layout){
            this.doLayout();
        }
        if(this.keyMap){
            this.keyMap.enable();
        }
		this.fireEvent("show", this);
		this.task.delay(this.hideDelay || 3000);
		if (this.closable) {
			this.task.cancel();
			this.on('close', function(){
				Ext.ux.NotifyMgr.positions.remove(this.pos);
				Ext.ux.NotifyMgr.windows.remove(this);
			}, this);
		}
	},
	animShow: function(){
		this.pos = 0;
		while (Ext.ux.NotifyMgr.positions.indexOf(this.pos) >- 1) this.pos++;
		Ext.ux.NotifyMgr.positions.push(this.pos);
		this.setZIndex(Ext.ux.NotifyMgr.zIndex);
		this.el.setXY([-9999, -9999]);
		this.el.show();
		this.msgWidth = (this.msgWidth && this.msgWidth > 0) ? this.msgWidth : 250;
		this.setWidth(this.msgWidth);
		var bh = Ext.getBody().getComputedHeight(),
			bw = Ext.getBody().getComputedWidth(),
			h = this.getInnerHeight() + this.getFrameHeight(),
			w = this.getInnerWidth() + this.getFrameWidth(),
			x, y, shift;
		switch (Ext.ux.NotifyMgr.alignment) {
			case 'top-left':
				x = Ext.ux.NotifyMgr.offsets[1] || 10;
				y = -h;
				shift = 0;
				break;
			case 'top-center':
				x = bw - parseInt(bw / 2) - parseInt(w / 2);
				y = -h;
				shift = 0;
				break;
			case 'top-right':
				x = bw - w - (Ext.ux.NotifyMgr.offsets[1] || 10);
				y = -h;
				shift = 0;
				break;
			case 'bottom-left':
				x = Ext.ux.NotifyMgr.offsets[1] || 10;
				y = bh;
				shift = bh - h;
				break;
			case 'bottom-center':
				x = bw - parseInt(bw / 2) - parseInt(w / 2);
				y = bh;
				shift = bh - h;
				break;
			case 'bottom-right':
				x = bw - w - (Ext.ux.NotifyMgr.offsets[1] || 10);
				y = bh;
				shift = bh - h;
				break;
		}
		this.el.setXY([x, y]);
		this.el.shift({
			y: shift - (Ext.ux.NotifyMgr.alignment.indexOf('bottom') > -1 ? Ext.ux.NotifyMgr.offsets[0] : -Ext.ux.NotifyMgr.offsets[0]),
			duration: 1,
			callback: this.afterShow,
			scope: this
		});
		if (Ext.ux.NotifyMgr.windows.length > 0) {
			for (var i = 0; i < Ext.ux.NotifyMgr.windows.length; ++i) {
				var offset = 0;
				for (var j = i; j < Ext.ux.NotifyMgr.windows.length; ++j) {
					offset += Ext.ux.NotifyMgr.windows[j].getInnerHeight() + Ext.ux.NotifyMgr.windows[j].getFrameHeight() + (Ext.ux.NotifyMgr.spacing || 10);
				}
				Ext.ux.NotifyMgr.windows[i].el.shift({
					concurrent: true,
					y: shift - (Ext.ux.NotifyMgr.alignment.indexOf('bottom') > -1 ? offset : -offset) - (Ext.ux.NotifyMgr.alignment.indexOf('bottom') > -1 ? Ext.ux.NotifyMgr.offsets[0] : -Ext.ux.NotifyMgr.offsets[0]),
					duration:1
				});
			}
		}
		Ext.ux.NotifyMgr.windows.push(this);
	},
	animHide: function(){
		Ext.ux.NotifyMgr.positions.remove(this.pos);
		Ext.ux.NotifyMgr.windows.remove(this);
		this.el.fadeOut({
			duration: 2,
			scope: this,
			block: true,
			stopFx: true,
			useDisplay:false,
			callback: this.destroy
		});
	}
});