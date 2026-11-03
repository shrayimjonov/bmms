import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { CanActivateUserRoute } from '../core/auth/can-activate-user-route';
import { Authority } from '../config/authority.constants';

export const DASHBOARD_ROUTES: Routes = [
  {
    path: '',
    component: DashboardComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'home',
      },
      {
        path: 'home',
        loadChildren: () => import('./entities/home/home.module').then(m => m.HomeModule),
        data: {
          pageTitle: 'global.menu.home',
          contentBgColor: '#f0f2f5',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'settings',
        loadChildren: () => import('./entities/settings/settings.module').then(m => m.SettingsModule),
        data: {
          pageTitle: 'global.menu.account.settings',
          noBreadcrumb: true,
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'user-management',
        loadChildren: () => import('./entities/user-management/user-management.module').then(m => m.UserManagementModule),
        data: {
          authorities: [
            Authority.USER_MANAGEMENT_READ,
            Authority.USER_MANAGEMENT_WRITE,
            Authority.USER_GROUP_READ,
            Authority.AUTHORITY_READ,
          ],
          noBreadcrumb: true,
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'p2p',
        loadChildren: () => import('./entities/p2p/p2p.module').then(m => m.P2pModule),
        data: {
          authorities: [
            Authority.P2P_COMMISSION_READ,
            Authority.P2P_COMMISSION_WRITE,
            Authority.P2P_CONDITION_READ,
            Authority.P2P_CONDITION_WRITE,
            Authority.P2P_PARAM_READ,
            Authority.P2P_PARAM_WRITE,
          ],
          noBreadcrumb: true,
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'transaction',
        loadChildren: () => import('./entities/transaction/transaction.module').then(m => m.TransactionModule),
        data: {
          authorities: [Authority.TRANSACTION_READ, Authority.TRANSACTION_WRITE, Authority.TRANSACTION_EDIT],
          pageTitle: 'billMasterGwApp.pkbillmsTransaction.home.title',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'agent',
        loadChildren: () => import('./entities/agent/agent.module').then(m => m.AgentModule),
        data: {
          authorities: [Authority.AGENT_READ, Authority.AGENT_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsAgent',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'recipient',
        loadChildren: () => import('./entities/recipient/recipient.module').then(m => m.RecipientModule),
        data: {
          authorities: [Authority.RECIPIENT_READ, Authority.RECIPIENT_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsRecipient',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'bill',
        loadChildren: () => import('./entities/bill/bill.module').then(m => m.BillModule),
        data: {
          authorities: [Authority.BILL_READ],
          pageTitle: 'global.menu.entities.pkbillmsBill',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'category',
        loadChildren: () => import('./entities/category/category.module').then(m => m.CategoryModule),
        data: {
          authorities: [Authority.CATEGORY_READ, Authority.CATEGORY_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsCategory',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'gateway',
        loadChildren: () => import('./entities/gateway/gateway.module').then(m => m.GatewayModule),
        data: {
          authorities: [Authority.GATEWAY_READ, Authority.GATEWAY_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsGateway',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'gateway-param',
        loadChildren: () => import('./entities/gateway-param/gateway-param.module').then(m => m.GatewayParamModule),
        data: {
          authorities: [Authority.GATEWAY_PARAM_READ, Authority.GATEWAY_PARAM_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsGatewayParam',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'adapter',
        loadChildren: () => import('./entities/adapter/adapter.module').then(m => m.AdapterModule),
        data: {
          authorities: [Authority.ADAPTER_READ, Authority.ADAPTER_WRITE],
          pageTitle: 'global.adapter.title',
        },
        canActivate: [CanActivateUserRoute],
      },
      // {
      //   path: 'report',
      //   loadChildren: () => import('./entities/report/report.module').then(m => m.ReportModule),
      //   data: {
      //     authorities: [Authority.REPORT_READ],
      //     pageTitle: 'global.menu.entities.reportmsReport',
      //   },
      //   canActivate: [CanActivateUserRoute],
      // },
      {
        path: 'prefix',
        loadChildren: () => import('./entities/prefix/prefix.module').then(m => m.PrefixModule),
        data: {
          authorities: [Authority.PREFIX_READ, Authority.PREFIX_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsPrefix',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'agent-recipient-setting',
        loadChildren: () =>
          import('./entities/agent-recipient-setting/agent-recipient-setting.module').then(m => m.AgentRecipientSettingModule),
        data: {
          authorities: [Authority.AGENT_RECIPIENT_SETTING_READ, Authority.AGENT_RECIPIENT_SETTING_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsSetting',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'bookkeeping-transaction',
        loadChildren: () =>
          import('./entities/bookkeeping-transaction/bookkeeping-transaction.module').then(m => m.BookkeepingTransactionModule),
        data: {
          authorities: [Authority.BOOKKEEPING_TRANSACTION_READ],
          pageTitle: 'global.menu.entities.pkbillmsBookkeepingTransaction',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'response-field',
        loadChildren: () => import('./entities/response-field/response-field.module').then(m => m.ResponseFieldModule),
        data: {
          authorities: [Authority.RESPONSE_FIELD_READ, Authority.RESPONSE_FIELD_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsResponseField',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'field-value',
        loadChildren: () => import('./entities/field-value/field-value.module').then(m => m.FieldValueModule),
        data: {
          authorities: [Authority.FIELD_VALUE_READ, Authority.FIELD_VALUE_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsFieldValue',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'field',
        loadChildren: () => import('./entities/field/field.module').then(m => m.FieldModule),
        data: {
          authorities: [Authority.FIELD_READ, Authority.FIELD_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsField',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'reconcile',
        loadChildren: () => import('./entities/reconcile/reconcile.module').then(m => m.ReconcileModule),
        data: {
          authorities: [Authority.RECONCILE_SUMMARY_READ, Authority.RECONCILE_TERMINAL_READ],
          pageTitle: 'global.menu.reconcile.main',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'recipient-group',
        loadChildren: () => import('./entities/recipient-group/recipient-group.module').then(m => m.RecipientGroupModule),
        data: {
          authorities: [Authority.RECIPIENT_GROUP_READ, Authority.RECIPIENT_GROUP_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsRecipientGroup',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'agent-recipient',
        loadChildren: () => import('./entities/agent-recipient/agent-recipient.module').then(m => m.AgentRecipientModule),
        data: {
          authorities: [Authority.AGENT_RECIPIENT_READ, Authority.AGENT_RECIPIENT_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsAgentRecipient',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'deposit-transaction',
        loadChildren: () => import('./entities/deposit-transaction/deposit-transaction.module').then(m => m.DepositTransactionModule),
        data: {
          authorities: [Authority.DEPOSIT_TRANSACTION_READ],
          pageTitle: 'billMasterGwApp.pkbillmsDepositTransaction.home.title',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'bookkeeping-template',
        loadChildren: () => import('./entities/bookkeeping-template/bookkeeping-template.module').then(m => m.BookkeepingTemplateModule),
        data: {
          authorities: [Authority.BOOKKEEPING_TEMPLATE_READ, Authority.BOOKKEEPING_TEMPLATE_WRITE],
          pageTitle: 'global.menu.entities.pkbillmsBookkeepingTemplate',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'deposit',
        loadChildren: () => import('./entities/deposit/deposit.module').then(m => m.DepositModule),
        data: {
          authorities: [Authority.DEPOSIT_READ, Authority.DEPOSIT_READ],
          pageTitle: 'global.menu.entities.pkbillmsDeposit',
        },
        canActivate: [CanActivateUserRoute],
      },
      // {
      //   path: 'faq',
      //   loadChildren: () => import('./entities/faq/faq.module').then(m => m.FaqModule),
      //   data: {
      //     authorities: [Authority.FAQ_READ, Authority.FAQ_WRITE],
      //     pageTitle: 'global.menu.entities.faq',
      //   },
      //   canActivate: [CanActivateUserRoute],
      // },
      // {
      //   path: 'faq-category',
      //   loadChildren: () => import('./entities/faq/category/faq-category.module').then(m => m.FaqCategoryModule),
      //   data: {
      //     authorities: [Authority.FAQ_CATEGORY_READ, Authority.FAQ_CATEGORY_WRITE],
      //     pageTitle: 'global.menu.entities.faqCategory',
      //   },
      //   canActivate: [CanActivateUserRoute],
      // },
      {
        path: 'recipient-file-schema',
        loadChildren: () => import('./entities/recipient-file-schema/recipient-file-schema.module').then(m => m.RecipientFileSchemaModule),
        data: {
          authorities: [Authority.RECIPIENT_FILE_SCHEMA_READ, Authority.RECIPIENT_FILE_SCHEMA_WRITE],
        },
        canActivate: [CanActivateUserRoute],
      },
      // {
      //   path: 'help',
      //   loadChildren: () => import('./entities/help/help.module').then(m => m.HelpModule),
      //   data: {
      //     authorities: [],
      //     pageTitle: 'global.menu.entities.help',
      //   },
      //   canActivate: [CanActivateUserRoute],
      // },
      {
        path: 'currency',
        loadChildren: () => import('./entities/currency/currency.module').then(m => m.CurrencyModule),
        data: {
          authorities: [Authority.CURRENCY_READ, Authority.CURRENCY_WRITE],
          pageTitle: 'global.menu.entities.bmmsCurrency',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'humo-bin',
        loadChildren: () => import('./entities/humo-bin/humo-bin.module').then(m => m.HumoBinModule),
        data: {
          authorities: [Authority.ADMIN],
          pageTitle: 'global.menu.entities.pkbillmsPcBin',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'docs',
        loadChildren: () => import('./entities/docs/docs.module').then(m => m.DocsModule),
        data: {
          authorities: [Authority.SWAGGER],
          pageTitle: 'global.menu.admin.apidocs',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'health',
        loadChildren: () => import('./entities/health/health.module').then(m => m.HealthModule),
        data: {
          authorities: [Authority.ADMIN],
          pageTitle: 'global.menu.admin.health',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'logs',
        loadChildren: () => import('./entities/logs/logs.module').then(m => m.LogsModule),
        data: {
          authorities: [Authority.ADMIN],
          pageTitle: 'global.menu.admin.logs',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'metrics',
        loadChildren: () => import('./entities/metrics/metrics.module').then(m => m.MetricsModule),
        data: {
          authorities: [Authority.ADMIN],
          pageTitle: 'global.menu.admin.metrics',
        },
        canActivate: [CanActivateUserRoute],
      },
      {
        path: 'cash-back',
        loadChildren: () => import('./entities/cash-back/cash-back.module').then(m => m.CashBackModule),
        data: {
          authorities: [Authority.ADMIN],
          pageTitle: 'global.menu.admin.cashBack',
        },
        canActivate: [CanActivateUserRoute],
      },
    ],
  },
];
