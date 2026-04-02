import { useMemo } from 'react';
import { Alert, Button, Empty, Modal, Skeleton, Tag, Typography } from 'antd';

import type { UserProfile } from '../../../types/auth';
import type { DocumentVersionResponse } from '../../../types/documents';
import { PREVIEW_EMPTY_HTML } from '../utils/editorHelpers';
import { buildRollbackComparison, type RollbackTimelineEntry } from '../utils/rollbackHelpers';

interface DocumentRollbackModalProps {
  currentContent: string;
  currentSequenceNumber: number;
  isLoadingRollbackHistory: boolean;
  isLoadingRollbackPreview: boolean;
  isRollingBack: boolean;
  isRollbackTargetInvalid: boolean;
  open: boolean;
  participants: Record<string, UserProfile>;
  rollbackPreview: DocumentVersionResponse | null;
  rollbackTargetSequence: number | null;
  rollbackTimeline: RollbackTimelineEntry[];
  onCancel: () => void;
  onConfirm: () => void;
  onSelectVersion: (sequenceNumber: number) => void;
}

export const DocumentRollbackModal = ({
  currentContent,
  currentSequenceNumber,
  isLoadingRollbackHistory,
  isLoadingRollbackPreview,
  isRollingBack,
  isRollbackTargetInvalid,
  open,
  participants,
  rollbackPreview,
  rollbackTargetSequence,
  rollbackTimeline,
  onCancel,
  onConfirm,
  onSelectVersion,
}: DocumentRollbackModalProps) => {
  const comparison = useMemo(
    () => (rollbackPreview ? buildRollbackComparison(currentContent, rollbackPreview.content) : null),
    [currentContent, rollbackPreview],
  );

  return (
    <Modal
      open={open}
      title="Rollback document"
      width={1180}
      onCancel={onCancel}
      footer={
        <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
          <Typography.Text className="!text-sm !text-slate-500">
            {rollbackTargetSequence !== null
              ? `Selected version: #${rollbackTargetSequence}`
              : 'Select a version card to inspect it before rollback.'}
          </Typography.Text>
          <div className="flex items-center justify-end gap-2">
            <Button onClick={onCancel}>Cancel</Button>
            <Button danger type="primary" disabled={isRollbackTargetInvalid} loading={isRollingBack} onClick={onConfirm}>
              {rollbackTargetSequence !== null ? `Rollback to #${rollbackTargetSequence}` : 'Rollback'}
            </Button>
          </div>
        </div>
      }
    >
      <div className="space-y-5">
        <Alert
          className="rollback-info-alert"
          message={`Current version: #${currentSequenceNumber}`}
          description="Choose a version card on the left, inspect the full document on the right, and then restore that state if it matches what you want."
          style={{
            background: 'rgba(240, 249, 255, 0.92)',
            borderColor: 'rgba(125, 211, 252, 0.65)',
          }}
          type="info"
          showIcon
        />

        <div className="grid gap-4 xl:grid-cols-[360px_minmax(0,1fr)]">
          <section className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
            <div className="mb-4 flex items-center justify-between gap-3">
              <div>
                <Typography.Title className="!mb-0 !text-lg !text-ink" level={5}>
                  Version cards
                </Typography.Title>
                <Typography.Text className="!text-sm !text-slate-500">
                  Click any older state to inspect it.
                </Typography.Text>
              </div>
              <Tag color="default">{rollbackTimeline.length} options</Tag>
            </div>

            {isLoadingRollbackHistory ? (
              <div className="space-y-3">
                {Array.from({ length: 4 }).map((_, index) => (
                  <div key={index} className="rounded-2xl border border-slate-200 bg-white p-4">
                    <Skeleton active paragraph={{ rows: 3 }} title={{ width: '60%' }} />
                  </div>
                ))}
              </div>
            ) : rollbackTimeline.length > 0 ? (
              <div className="max-h-[560px] space-y-3 overflow-y-auto pr-1">
                {rollbackTimeline.map((entry) => {
                  const isSelected = rollbackTargetSequence === entry.sequenceNumber;
                  const authorLabel = entry.userId
                    ? participants[entry.userId]?.name ?? participants[entry.userId]?.email ?? entry.userId
                    : 'System';
                  const entryTypeLabel =
                    entry.type === 'INITIAL' ? 'Initial state' : entry.type === 'DELETE' ? 'Deletion' : 'Insertion';
                  const entryTypeColor =
                    entry.type === 'INITIAL' ? 'default' : entry.type === 'DELETE' ? 'volcano' : 'green';

                  return (
                    <button
                      key={entry.sequenceNumber}
                      className={`w-full rounded-2xl border p-4 text-left transition ${
                        isSelected
                          ? 'border-cyan-300 bg-cyan-50 shadow-soft'
                          : 'border-slate-200 bg-white hover:border-emerald-200 hover:bg-emerald-50/40'
                      }`}
                      onClick={() => onSelectVersion(entry.sequenceNumber)}
                      type="button"
                    >
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <div className="flex flex-wrap items-center gap-2">
                          <Tag color="blue">Version #{entry.sequenceNumber}</Tag>
                          <Tag color={entryTypeColor}>{entryTypeLabel}</Tag>
                        </div>
                        {isSelected ? <Tag color="cyan">Selected</Tag> : null}
                      </div>

                      <Typography.Paragraph className="!mb-1 !mt-3 !font-medium !text-ink">
                        {entry.summary}
                      </Typography.Paragraph>
                      <Typography.Paragraph className="!mb-3 !text-sm !leading-6 !text-slate-600">
                        {entry.contentPreview}
                      </Typography.Paragraph>

                      <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-slate-500">
                        <span>{entry.changePreview}</span>
                        <span>
                          {authorLabel} | {entry.targetVisibleLength} chars
                        </span>
                      </div>
                    </button>
                  );
                })}
              </div>
            ) : (
              <Empty
                description="There are no older versions available for rollback yet."
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            )}
          </section>

          <section className="rounded-3xl border border-slate-200 bg-slate-50 p-4">
            <div className="mb-4 flex flex-wrap items-start justify-between gap-3">
              <div>
                <Typography.Title className="!mb-0 !text-lg !text-ink" level={5}>
                  Full version preview
                </Typography.Title>
                <Typography.Text className="!text-sm !text-slate-500">
                  Review the complete document state before restoring it.
                </Typography.Text>
              </div>
              <Tag color={rollbackPreview ? 'blue' : 'default'}>
                {rollbackPreview ? `Version #${rollbackPreview.sequenceNumber}` : 'No version selected'}
              </Tag>
            </div>

            {rollbackPreview && comparison ? (
              <div className="mb-4 grid gap-3 md:grid-cols-3">
                <div className="rounded-2xl border border-slate-200 bg-white p-4">
                  <Typography.Text className="!text-xs !uppercase !tracking-[0.2em] !text-slate-400">
                    Visible Text Delta
                  </Typography.Text>
                  <div className="mt-2 text-lg font-semibold text-ink">
                    {comparison.visibleDelta === 0
                      ? 'No text delta'
                      : comparison.visibleDelta > 0
                        ? `+${comparison.visibleDelta} chars`
                        : `${comparison.visibleDelta} chars`}
                  </div>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-4">
                  <Typography.Text className="!text-xs !uppercase !tracking-[0.2em] !text-slate-400">
                    Formatting
                  </Typography.Text>
                  <div className="mt-2 text-lg font-semibold text-ink">
                    {comparison.formattingChanged ? 'Changed' : 'Matches text state'}
                  </div>
                </div>
                <div className="rounded-2xl border border-slate-200 bg-white p-4">
                  <Typography.Text className="!text-xs !uppercase !tracking-[0.2em] !text-slate-400">
                    Target Length
                  </Typography.Text>
                  <div className="mt-2 text-lg font-semibold text-ink">{comparison.targetVisibleLength} chars</div>
                </div>
              </div>
            ) : null}

            {isLoadingRollbackPreview ? (
              <div className="rounded-2xl border border-slate-200 bg-white p-5">
                <Skeleton active paragraph={{ rows: 10 }} />
              </div>
            ) : rollbackPreview ? (
              <div
                className="rich-text-content max-h-[560px] overflow-y-auto rounded-2xl border border-slate-200 bg-white p-5 text-[15px] leading-7 text-ink"
                dangerouslySetInnerHTML={{
                  __html: rollbackPreview.content?.trim() ? rollbackPreview.content : PREVIEW_EMPTY_HTML,
                }}
              />
            ) : (
              <Empty
                description="Select a version card to load its full document preview."
                image={Empty.PRESENTED_IMAGE_SIMPLE}
              />
            )}
          </section>
        </div>
      </div>
    </Modal>
  );
};
