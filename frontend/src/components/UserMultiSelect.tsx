import { useEffect, useMemo, useRef, useState } from 'react';
import { Select } from 'antd';

import { usersApi } from '../api/users';
import type { UserProfile } from '../types/auth';

interface UserMultiSelectProps {
  value?: string[];
  onChange?: (value: string[]) => void;
  seedUsers?: UserProfile[];
  excludedUserIds?: string[];
}

const EMPTY_USERS: UserProfile[] = [];
const EMPTY_IDS: string[] = [];

export const UserMultiSelect = ({ value = [], onChange, seedUsers, excludedUserIds }: UserMultiSelectProps) => {
  const normalizedSeedUsers = seedUsers ?? EMPTY_USERS;
  const normalizedExcludedUserIds = excludedUserIds ?? EMPTY_IDS;
  const [options, setOptions] = useState<UserProfile[]>(normalizedSeedUsers);
  const [fetching, setFetching] = useState(false);
  const searchTimeoutRef = useRef<number | null>(null);
  const latestRequestRef = useRef(0);

  const mergeOptions = (users: UserProfile[]) => {
    setOptions((current) => {
      const map = new Map(current.map((item) => [item.id, item]));
      users.forEach((user) => map.set(user.id, user));
      const nextOptions = Array.from(map.values());

      if (nextOptions.length === current.length && nextOptions.every((user, index) => user.id === current[index]?.id)) {
        return current;
      }

      return nextOptions;
    });
  };

  useEffect(() => {
    if (normalizedSeedUsers.length === 0) {
      return;
    }

    mergeOptions(normalizedSeedUsers);
  }, [normalizedSeedUsers]);

  useEffect(
    () => () => {
      if (searchTimeoutRef.current !== null) {
        window.clearTimeout(searchTimeoutRef.current);
      }
    },
    [],
  );

  const selectOptions = useMemo(
    () =>
      options
        .filter((user) => !normalizedExcludedUserIds.includes(user.id))
        .map((user) => ({
          label: `${user.name} (${user.email})`,
          value: user.id,
        })),
    [normalizedExcludedUserIds, options],
  );

  const performSearch = async (search: string) => {
    const requestId = latestRequestRef.current + 1;
    latestRequestRef.current = requestId;
    setFetching(true);

    try {
      const response = await usersApi.searchByName(search, 0, 10);
      if (latestRequestRef.current !== requestId) {
        return;
      }

      mergeOptions(response.content);
    } finally {
      if (latestRequestRef.current === requestId) {
        setFetching(false);
      }
    }
  };

  const handleSearch = (search: string) => {
    if (searchTimeoutRef.current !== null) {
      window.clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = window.setTimeout(() => {
      searchTimeoutRef.current = null;
      void performSearch(search);
    }, 250);
  };

  return (
    <Select
      mode="multiple"
      allowClear
      placeholder="Search users by name"
      showSearch
      filterOption={false}
      value={value}
      options={selectOptions}
      onChange={onChange}
      onSearch={handleSearch}
      onOpenChange={(open) => {
        if (open && options.length === 0) {
          if (searchTimeoutRef.current !== null) {
            window.clearTimeout(searchTimeoutRef.current);
            searchTimeoutRef.current = null;
          }

          void performSearch('');
        }
      }}
      loading={fetching}
      optionFilterProp="label"
      size="large"
    />
  );
};
