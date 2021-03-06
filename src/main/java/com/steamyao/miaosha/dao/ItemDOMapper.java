package com.steamyao.miaosha.dao;

import com.steamyao.miaosha.dataobject.ItemDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ItemDOMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Mon Jul 22 15:13:59 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Mon Jul 22 15:13:59 CST 2019
     */
    int insert(ItemDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Mon Jul 22 15:13:59 CST 2019
     */
    int insertSelective(ItemDO record);

    List<ItemDO> listItem();


    void increaseSales(@Param("id")Integer id, @Param("amount")Integer amount);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Mon Jul 22 15:13:59 CST 2019
     */
    ItemDO selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Mon Jul 22 15:13:59 CST 2019
     */
    int updateByPrimaryKeySelective(ItemDO record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table item
     *
     * @mbg.generated Mon Jul 22 15:13:59 CST 2019
     */
    int updateByPrimaryKey(ItemDO record);
}