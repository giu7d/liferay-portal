/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import './ViolationPopover.scss';

import ClayIcon from '@clayui/icon';
import ClayList from '@clayui/list';
import ClayPopover from '@clayui/popover';
import React, {useCallback, useMemo, useState} from 'react';

import {useObserveRect} from '../hooks/useObserveRect';

import type {RuleRaw} from '../hooks/useA11y';

const Overlay = React.forwardRef<
	HTMLDivElement,
	React.ButtonHTMLAttributes<HTMLDivElement>
>(({style, ...othersProps}, ref) => (
	<div {...othersProps} className="a11y-overlay" ref={ref} style={style}>
		<div className="a11y-indicator">
			<ClayIcon symbol="info-circle" />
		</div>

		<div className="a11y-backdrop" />
	</div>
));

type ViolationProps = {
	onClick: (target: string, id: string) => void;
	rules: Record<string, RuleRaw>;
	target: string;
	violations: Array<string>;
};

export function ViolationPopover({
	onClick,
	rules,
	target,
	violations,
}: ViolationProps) {
	const [visible, setVisible] = useState(false);
	const [bounds, setBounds] = useState<React.CSSProperties>();

	const node = useMemo(() => document.querySelector(target), [target]);

	useObserveRect(
		useCallback(
			(bounds) => {
				bounds = bounds ?? (node as Element).getBoundingClientRect();

				setBounds({
					height: bounds.height,
					left: bounds.left,
					top: bounds.top,
					width: bounds.width,
				});
			},
			[node]
		),
		node
	);

	return (
		<ClayPopover
			className="a11y-popover"
			header={
				<>
					<div className="inline-item">
						<ClayIcon
							className="text-danger"
							symbol="info-circle"
						/>
					</div>

					<div className="inline-item inline-item-after">
						<span>
							{Liferay.Language.get('accessibility-violations')}
						</span>
					</div>
				</>
			}
			onShowChange={setVisible}
			show={visible}
			trigger={<Overlay style={bounds} />}
		>
			<div className="list-group">
				{violations.map((ruleId) => (
					<button
						className="list-group-item list-group-item-action list-group-item-flex list-group-item-flush"
						key={ruleId}
						onClick={() => onClick(target, ruleId)}
					>
						<ClayList.ItemField expand>
							<ClayList.ItemTitle>
								{ruleId}{' '}
								<span className="text-secondary">{`- ${rules[ruleId].impact}`}</span>
							</ClayList.ItemTitle>

							<ClayList.ItemText subtext>
								{rules[ruleId].help}
							</ClayList.ItemText>
						</ClayList.ItemField>

						<ClayList.ItemField>
							<ClayIcon
								className="text-secondary"
								symbol="angle-right"
							/>
						</ClayList.ItemField>
					</button>
				))}
			</div>
		</ClayPopover>
	);
}
