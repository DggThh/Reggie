package com.cy.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.reggie.entity.AddressBook;
import com.cy.reggie.mapper.AddressBookMapper;
import com.cy.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl  extends ServiceImpl<AddressBookMapper,AddressBook> implements AddressBookService {
}
