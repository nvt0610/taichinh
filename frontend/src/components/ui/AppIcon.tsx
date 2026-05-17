import type { ComponentProps } from 'react';
import type { LucideProps } from 'lucide-react';

import { appIcons, type AppIconName } from '@/config/appIcons';

interface AppIconProps extends Omit<ComponentProps<'svg'>, 'name'>, LucideProps {
  name: AppIconName;
}

export function AppIcon({ name, ...props }: AppIconProps) {
  const Icon = appIcons[name];
  return <Icon aria-hidden="true" {...props} />;
}
