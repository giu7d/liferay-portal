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

package com.liferay.content.dashboard.web.internal.item;

import com.liferay.asset.kernel.model.AssetEntry;
import com.liferay.asset.kernel.service.AssetEntryLocalService;
import com.liferay.content.dashboard.web.internal.configuration.FFContentDashboardDocumentConfiguration;
import com.liferay.content.dashboard.web.internal.item.action.ContentDashboardItemActionProviderTracker;
import com.liferay.content.dashboard.web.internal.item.type.ContentDashboardItemTypeFactory;
import com.liferay.content.dashboard.web.internal.item.type.ContentDashboardItemTypeFactoryTracker;
import com.liferay.document.library.kernel.model.DLFileEntry;
import com.liferay.document.library.kernel.model.DLFileEntryType;
import com.liferay.document.library.kernel.service.DLAppLocalService;
import com.liferay.info.item.InfoItemServiceTracker;
import com.liferay.info.item.provider.InfoItemFieldValuesProvider;
import com.liferay.portal.configuration.metatype.bnd.util.ConfigurableUtil;
import com.liferay.portal.kernel.exception.NoSuchModelException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.Language;
import com.liferay.portal.kernel.repository.model.FileEntry;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.util.Portal;

import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author Alejandro Tardín
 */
@Component(
	configurationPid = "com.liferay.content.dashboard.web.internal.configuration.FFContentDashboardDocumentConfiguration",
	service = ContentDashboardItemFactory.class
)
public class FileEntryContentDashboardItemFactory
	implements ContentDashboardItemFactory<FileEntry> {

	@Override
	public ContentDashboardItem<FileEntry> create(long classPK)
		throws PortalException {

		FileEntry fileEntry = _dlAppLocalService.getFileEntry(classPK);

		AssetEntry assetEntry = _assetEntryLocalService.fetchEntry(
			DLFileEntry.class.getName(), fileEntry.getFileEntryId());

		if (assetEntry == null) {
			throw new NoSuchModelException(
				"Unable to find an asset entry for file entry " +
					fileEntry.getPrimaryKey());
		}

		InfoItemFieldValuesProvider<FileEntry> infoItemFieldValuesProvider =
			infoItemServiceTracker.getFirstInfoItemService(
				InfoItemFieldValuesProvider.class, FileEntry.class.getName());

		Optional<ContentDashboardItemTypeFactory>
			contentDashboardItemTypeFactoryOptional =
				_contentDashboardItemTypeFactoryTracker.
					getContentDashboardItemTypeFactoryOptional(
						DLFileEntryType.class.getName());

		ContentDashboardItemTypeFactory contentDashboardItemTypeFactory =
			contentDashboardItemTypeFactoryOptional.orElseThrow(
				NoSuchModelException::new);

		DLFileEntry dlFileEntry = (DLFileEntry)fileEntry.getModel();

		return new FileEntryContentDashboardItem(
			assetEntry.getCategories(), assetEntry.getTags(),
			_contentDashboardItemActionProviderTracker,
			contentDashboardItemTypeFactory.create(
				dlFileEntry.getFileEntryTypeId()),
			fileEntry, _groupLocalService.fetchGroup(fileEntry.getGroupId()),
			infoItemFieldValuesProvider, _language, _portal);
	}

	@Override
	public boolean isEnabled() {
		return _ffContentDashboardDocumentConfiguration.enabled();
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_ffContentDashboardDocumentConfiguration =
			ConfigurableUtil.createConfigurable(
				FFContentDashboardDocumentConfiguration.class, properties);
	}

	@Reference
	protected InfoItemServiceTracker infoItemServiceTracker;

	@Reference
	private AssetEntryLocalService _assetEntryLocalService;

	@Reference
	private ContentDashboardItemActionProviderTracker
		_contentDashboardItemActionProviderTracker;

	@Reference
	private ContentDashboardItemTypeFactoryTracker
		_contentDashboardItemTypeFactoryTracker;

	@Reference
	private DLAppLocalService _dlAppLocalService;

	private volatile FFContentDashboardDocumentConfiguration
		_ffContentDashboardDocumentConfiguration;

	@Reference
	private GroupLocalService _groupLocalService;

	@Reference
	private Language _language;

	@Reference
	private Portal _portal;

	@Reference
	private UserLocalService _userLocalService;

}