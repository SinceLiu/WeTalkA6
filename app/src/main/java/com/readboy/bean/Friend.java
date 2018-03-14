package com.readboy.bean;


/**
 *
 * @author hwwjian
 * @date 2016/9/21
 * 好友
 */
public class Friend{
	
	/**
     * 通讯录的查询Id
	 */
	public int contactId;
    /**
     * 用户名
     */
    public String name;

    /**
     * 用户的唯一标识(UUID)
     */
    public String uuid;
    /**
     * 用户头像
     */
    public byte[] avatar;
    /**
     * 用户的未读信息数
     */
    public int unreadCount;
    /**
     * 短号
     */
    public String shortPhone;
    /**
     * 手机号码
     */
    public String phone;
    /**
     * 关系
     */
    public int relation;
    /**
     * 头像uri
     */
    public String photoUri;

    /**
     * 机型
     */
    public Model model;
    
    @Override
	public String toString() {
		return "WTContact [contactId=" + contactId + ", name=" + name
				+ ", uuid=" + uuid + ", unreadCount=" + unreadCount + ","
				+ ", shortPhone=" + shortPhone + ", phone=" + phone + ",relation=" + relation + "]";
	}
}
