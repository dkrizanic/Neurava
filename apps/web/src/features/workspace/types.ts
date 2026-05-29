import type { WorkspaceSummary } from '../auth/types';

export type CompanyRegistration = {
  businessWorkspace: WorkspaceSummary;
  company: {
    id: string;
    name: string;
  };
};
