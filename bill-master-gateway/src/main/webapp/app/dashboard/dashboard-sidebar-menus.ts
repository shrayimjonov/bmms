import { Authority } from '../config/authority.constants';

export class Menu<VALUE_TYPE = any> {
  title: string;
  routerLink?: string;
  authorities?: Authority[];
  notForAdmin?: boolean;
  icon?: string;
  children?: Menu[];
  value?: VALUE_TYPE;
}

export const DASHBOARD_SIDEBAR_MENUS: Menu[] = [
  {
    title: 'global.menu.home',
    icon: 'home',
    routerLink: 'home',
  },
  {
    title: 'global.menu.entities.pkbillmsTransaction',
    icon: 'transaction',
    routerLink: 'transaction',
    authorities: [Authority.TRANSACTION_READ, Authority.TRANSACTION_WRITE],
  },
  {
    title: 'global.menu.reconcile.main',
    icon: 'reconciliation',
    routerLink: 'reconcile',
    authorities: [Authority.RECONCILE_SUMMARY_READ, Authority.RECONCILE_TERMINAL_READ],
  },
  {
    title: 'global.menu.entities.userManagement',
    icon: 'users',
    routerLink: 'user-management',
    authorities: [Authority.USER_MANAGEMENT_READ, Authority.USER_MANAGEMENT_WRITE, Authority.USER_GROUP_READ, Authority.AUTHORITY_READ],
  },
  {
    title: 'global.menu.entities.p2p',
    icon: 'p2p',
    routerLink: 'p2p',
    authorities: [
      Authority.P2P_COMMISSION_READ,
      Authority.P2P_COMMISSION_WRITE,
      Authority.P2P_CONDITION_READ,
      Authority.P2P_CONDITION_WRITE,
      Authority.P2P_PARAM_READ,
      Authority.P2P_PARAM_WRITE,
    ],
  },
  {
    title: 'global.menu.submenu.agent.main',
    icon: 'agent',
    authorities: [
      Authority.AGENT_READ,
      Authority.AGENT_WRITE,
      Authority.DEPOSIT_READ,
      Authority.DEPOSIT_READ,
      Authority.DEPOSIT_TRANSACTION_READ,
    ],
    children: [
      {
        title: 'global.menu.submenu.agent.agents',
        icon: 'agent',
        routerLink: 'agent',
        authorities: [Authority.AGENT_READ, Authority.AGENT_WRITE],
      },
      {
        title: 'global.menu.submenu.agent.deposits',
        icon: 'wallet',
        routerLink: 'deposit',
        authorities: [Authority.DEPOSIT_READ, Authority.DEPOSIT_READ],
      },
      {
        title: 'global.menu.submenu.agent.depositOperations',
        icon: 'deposit-transaction',
        routerLink: 'deposit-transaction',
        authorities: [Authority.DEPOSIT_TRANSACTION_READ],
      },
    ],
  },
  {
    title: 'global.menu.submenu.gateway.main',
    icon: 'gateway',
    authorities: [
      Authority.BILL_READ,
      Authority.GATEWAY_READ,
      Authority.GATEWAY_WRITE,
      Authority.GATEWAY_PARAM_READ,
      Authority.GATEWAY_PARAM_WRITE,
    ],
    children: [
      {
        title: 'global.menu.submenu.gateway.gateways',
        icon: 'gateway',
        routerLink: 'gateway',
        authorities: [Authority.GATEWAY_READ, Authority.GATEWAY_WRITE],
      },
      {
        title: 'global.menu.submenu.gateway.bills',
        icon: 'bill',
        routerLink: 'bill',
        authorities: [Authority.BILL_READ],
      },
      {
        title: 'global.menu.submenu.gateway.params',
        icon: 'tool',
        routerLink: 'gateway-param',
        authorities: [Authority.GATEWAY_PARAM_READ, Authority.GATEWAY_PARAM_WRITE],
      },
    ],
  },
  {
    title: 'global.menu.submenu.recipient.main',
    icon: 'agent',
    authorities: [
      Authority.RECIPIENT_READ,
      Authority.RECIPIENT_WRITE,
      Authority.AGENT_RECIPIENT_READ,
      Authority.AGENT_RECIPIENT_WRITE,
      Authority.RECIPIENT_GROUP_READ,
      Authority.RECIPIENT_GROUP_WRITE,
      Authority.AGENT_RECIPIENT_SETTING_READ,
      Authority.AGENT_RECIPIENT_SETTING_WRITE,
      Authority.RECIPIENT_FILE_SCHEMA_READ,
      Authority.RECIPIENT_FILE_SCHEMA_WRITE,
    ],
    children: [
      {
        title: 'global.menu.submenu.recipient.categories',
        icon: 'tags',
        routerLink: 'category',
        authorities: [Authority.CATEGORY_READ, Authority.CATEGORY_WRITE],
      },
      {
        title: 'global.menu.entities.pkbillmsRecipient',
        icon: 'recipients',
        routerLink: 'recipient',
        authorities: [Authority.RECIPIENT_READ, Authority.RECIPIENT_WRITE],
      },
      {
        title: 'global.menu.submenu.recipient.available',
        icon: 'recipients',
        routerLink: 'agent-recipient',
        authorities: [Authority.AGENT_RECIPIENT_READ, Authority.AGENT_RECIPIENT_WRITE],
      },
      {
        title: 'global.menu.submenu.recipient.groups',
        icon: 'group',
        routerLink: 'recipient-group',
        authorities: [Authority.RECIPIENT_GROUP_READ, Authority.RECIPIENT_GROUP_WRITE],
      },
      {
        title: 'global.menu.submenu.recipient.settings',
        icon: 'tool',
        routerLink: 'agent-recipient-setting',
        authorities: [Authority.AGENT_RECIPIENT_SETTING_READ, Authority.AGENT_RECIPIENT_SETTING_WRITE],
      },
      {
        title: 'global.menu.submenu.recipient.schemas',
        icon: 'schema',
        routerLink: 'recipient-file-schema',
        authorities: [Authority.RECIPIENT_FILE_SCHEMA_READ, Authority.RECIPIENT_FILE_SCHEMA_WRITE],
      },
    ],
  },
  {
    title: 'global.menu.submenu.field.main',
    icon: 'tags',
    authorities: [
      Authority.CATEGORY_READ,
      Authority.CATEGORY_WRITE,
      Authority.RECIPIENT_READ,
      Authority.RECIPIENT_WRITE,
      Authority.FIELD_VALUE_READ,
      Authority.FIELD_VALUE_WRITE,
      Authority.RESPONSE_FIELD_READ,
      Authority.RESPONSE_FIELD_WRITE,
      Authority.PREFIX_READ,
      Authority.PREFIX_WRITE,
      Authority.FIELD_READ,
      Authority.FIELD_WRITE,
    ],
    children: [
      {
        title: 'global.menu.submenu.field.fields',
        icon: 'user-tag',
        routerLink: 'field',
        authorities: [Authority.FIELD_READ, Authority.FIELD_WRITE],
      },
      {
        title: 'global.menu.submenu.field.responseFields',
        icon: 'tags',
        routerLink: 'response-field',
        authorities: [Authority.RESPONSE_FIELD_READ, Authority.RESPONSE_FIELD_WRITE],
      },
      {
        title: 'global.menu.submenu.field.values',
        icon: 'tags',
        routerLink: 'field-value',
        authorities: [Authority.FIELD_VALUE_READ, Authority.FIELD_VALUE_WRITE],
      },
      {
        title: 'global.menu.submenu.field.prefixes',
        icon: 'hashtag',
        routerLink: 'prefix',
        authorities: [Authority.PREFIX_READ, Authority.PREFIX_WRITE],
      },
    ],
  },
  {
    title: 'global.menu.submenu.bookkeeping.main',
    icon: 'bookkeeping',
    authorities: [Authority.BOOKKEEPING_TRANSACTION_READ, Authority.BOOKKEEPING_TEMPLATE_READ, Authority.BOOKKEEPING_TEMPLATE_WRITE],
    children: [
      {
        title: 'global.menu.submenu.bookkeeping.templates',
        icon: 'template',
        routerLink: 'bookkeeping-template',
        authorities: [Authority.BOOKKEEPING_TEMPLATE_READ, Authority.BOOKKEEPING_TEMPLATE_WRITE],
      },
      {
        title: 'global.menu.submenu.bookkeeping.operations',
        icon: 'book-transactions',
        routerLink: 'bookkeeping-transaction',
        authorities: [Authority.BOOKKEEPING_TRANSACTION_READ],
      },
    ],
  },
  {
    title: 'global.menu.submenu.other.main',
    icon: 'tags',
    authorities: [Authority.ADAPTER_READ, Authority.ADAPTER_WRITE, Authority.CURRENCY_READ, Authority.CURRENCY_WRITE, Authority.SWAGGER],
    children: [
      {
        title: 'global.menu.entities.cashBack',
        icon: 'dollar',
        routerLink: 'cash-back',
        // authorities: [Authority., Authority.FIELD_WRITE],
      },
      {
        title: 'global.menu.submenu.other.adapters',
        icon: 'tags',
        routerLink: 'adapter',
        authorities: [Authority.ADAPTER_READ, Authority.ADAPTER_WRITE],
      },
      {
        title: 'global.menu.submenu.other.currencies',
        icon: 'currency',
        routerLink: 'currency',
        authorities: [Authority.CURRENCY_READ, Authority.CURRENCY_WRITE],
      },
      {
        title: 'global.menu.submenu.other.api',
        icon: 'api',
        routerLink: 'docs',
        authorities: [Authority.SWAGGER],
      },
      {
        title: 'global.menu.entities.pkbillmsPcBin',
        icon: 'p2p',
        routerLink: 'humo-bin',
        authorities: [Authority.ADMIN],
      },
      {
        title: 'global.menu.admin.metrics',
        icon: 'dashboard',
        routerLink: 'metrics',
        authorities: [Authority.ADMIN],
      },
      {
        title: 'global.menu.admin.health',
        icon: 'heart',
        routerLink: 'health',
        authorities: [Authority.ADMIN],
      },
      {
        title: 'global.menu.admin.logs',
        icon: 'file-text',
        routerLink: 'logs',
        authorities: [Authority.ADMIN],
      },
    ],
  },
  // {
  //   title: 'global.menu.entities.faq',
  //   icon: 'tags',
  //   authorities: [Authority.FAQ_READ, Authority.FAQ_WRITE, Authority.FAQ_CATEGORY_READ, Authority.FAQ_CATEGORY_WRITE],
  //   children: [
  //     {
  //       title: 'global.menu.entities.faq',
  //       icon: 'tags',
  //       routerLink: 'faq',
  //       authorities: [Authority.FAQ_READ, Authority.FAQ_WRITE],
  //     },
  //     {
  //       title: 'global.menu.entities.faqCategory',
  //       icon: 'tags',
  //       routerLink: 'faq-category',
  //       authorities: [Authority.FAQ_CATEGORY_READ, Authority.FAQ_CATEGORY_WRITE],
  //     },
  //   ],
  // },
  // {
  //   title: 'global.menu.entities.reportmsReport',
  //   icon: 'report',
  //   routerLink: 'report',
  //   authorities: [Authority.REPORT_READ],
  // },
  // {
  //   title: 'global.menu.entities.help',
  //   icon: 'help',
  //   routerLink: 'help',
  //   authorities: [Authority.FAQ_CATEGORY_READ, Authority.FAQ_CATEGORY_WRITE],
  // },
];
