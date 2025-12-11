import { useEffect, useState, useMemo } from 'react';
import {
  Container,
  Typography,
  Button,
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  MenuItem,
  Chip,
  CircularProgress,
  Alert,
  FormControl,
  InputLabel,
  Select,
} from '@mui/material';
import { Add } from '@mui/icons-material';
import { promotionService } from '@/services/promotionService';
import { gradeService } from '@/services/gradeService';
import { goalService } from '@/services/goalService';
import { PromotionRequest, PromotionRequestData, Grade, Goal } from '@/types';
import { useAuth } from '@/contexts/AuthContext';
import { format } from 'date-fns';

const PromotionRequestPage = () => {
  const [promotionRequests, setPromotionRequests] = useState<PromotionRequest[]>([]);
  const [grades, setGrades] = useState<Grade[]>([]);
  const [completedGoals, setCompletedGoals] = useState<Goal[]>([]);
  const [attachedGoals, setAttachedGoals] = useState<Goal[]>([]);
  const [attachedFiles, setAttachedFiles] = useState<any[]>([]);
  const [selectedGoalsIds, setSelectedGoalsIds] = useState<number[]>([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [openDetailDialog, setOpenDetailDialog] = useState(false);
  const [openCommentDialog, setOpenCommentDialog] = useState(false);
  const [openGoalsDialog, setOpenGoalsDialog] = useState(false);
  const [selectedRequest, setSelectedRequest] = useState<PromotionRequest | null>(null);
  const [editingRequest, setEditingRequest] = useState<PromotionRequest | null>(null);
  const [gradeFilter, setGradeFilter] = useState('');
  const [periodFilter, setPeriodFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [hrComment, setHrComment] = useState('');
  const { user } = useAuth();
  const userRole = user?.role?.toLowerCase() || '';
  const isHR = userRole === 'hr';
  const isTeamLead = userRole === 'team_lead';
  const isEmployee = userRole === 'employee';
  const canCreateRequest = isEmployee || isTeamLead;

  const [formData, setFormData] = useState<PromotionRequestData>({
    employeeId: user?.employeeId || 0,
    requestedGradeId: 0,
    justification: '',
    evidence: '',
    reviewPeriod: 'Q4-2024',
  });

  useEffect(() => {
    fetchPromotionRequests();
    fetchGrades();
    if (canCreateRequest) {
      fetchCompletedGoals();
    }
  }, [user]);

  const fetchPromotionRequests = async () => {
    try {
      setLoading(true);
      if (isHR) {
        const data = await promotionService.getAllPromotionRequests();
        setPromotionRequests(data);
      } else if (user?.employeeId) {
        const data = await promotionService.getPromotionRequestsByEmployeeId(user.employeeId);
        setPromotionRequests(data);
      }
    } catch (error) {
      console.error('Failed to fetch promotion requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchGrades = async () => {
    try {
      const data = await gradeService.getAllGrades();
      setGrades(data);
    } catch (error) {
      console.error('Failed to fetch grades:', error);
    }
  };

  const fetchCompletedGoals = async () => {
    try {
      const data = await goalService.getMyCompletedGoals();
      setCompletedGoals(data);
    } catch (error) {
      console.error('Failed to fetch completed goals:', error);
    }
  };

  const fetchAttachedGoals = async (promotionRequestId: number) => {
    try {
      const data = await promotionService.getPromotionRequestGoals(promotionRequestId);
      setAttachedGoals(data);
    } catch (error) {
      console.error('Failed to fetch attached goals:', error);
      setAttachedGoals([]);
    }
  };

  const fetchAttachedFiles = async (promotionRequestId: number) => {
    try {
      const data = await promotionService.getPromotionRequestFiles(promotionRequestId);
      setAttachedFiles(data);
    } catch (error) {
      console.error('Failed to fetch attached files:', error);
      setAttachedFiles([]);
    }
  };

  // Get unique values for filters
  const uniquePeriods = useMemo(() => {
    const periods = new Set(promotionRequests.map(r => r.reviewPeriod));
    return Array.from(periods).sort();
  }, [promotionRequests]);

  const uniqueStatuses = useMemo(() => {
    const statuses = new Set(promotionRequests.map(r => r.status));
    return Array.from(statuses);
  }, [promotionRequests]);

  // Apply filters
  const filteredRequests = useMemo(() => {
    return promotionRequests.filter(request => {
      const matchesGrade = gradeFilter === '' || request.requestedGradeId === Number(gradeFilter);
      const matchesPeriod = periodFilter === '' || request.reviewPeriod === periodFilter;
      const matchesStatus = statusFilter === '' || request.status === statusFilter;
      
      return matchesGrade && matchesPeriod && matchesStatus;
    });
  }, [promotionRequests, gradeFilter, periodFilter, statusFilter]);

  const handleOpenDialog = () => {
    setEditingRequest(null);
    setSelectedGoalsIds([]);
    setFormData({
      employeeId: user?.employeeId || 0,
      requestedGradeId: 0,
      justification: '',
      evidence: '',
      reviewPeriod: 'Q4-2024',
    });
    setOpenDialog(true);
  };

  const handleEditRequest = async (request: PromotionRequest) => {
    setEditingRequest(request);
    setFormData({
      employeeId: request.employeeId,
      requestedGradeId: request.requestedGradeId,
      justification: request.justification,
      evidence: request.evidence,
      reviewPeriod: request.reviewPeriod,
    });
    
    // Load attached goals for this request
    try {
      const goals = await promotionService.getPromotionRequestGoals(request.id);
      setAttachedGoals(goals);
      setSelectedGoalsIds(goals.map(g => g.id));
    } catch (error) {
      console.error('Failed to load attached goals:', error);
      setAttachedGoals([]);
      setSelectedGoalsIds([]);
    }
    
    // Load attached files for this request
    try {
      const files = await promotionService.getPromotionRequestFiles(request.id);
      setAttachedFiles(files);
    } catch (error) {
      console.error('Failed to load attached files:', error);
      setAttachedFiles([]);
    }
    
    setOpenDialog(true);
  };

  const handleDeleteRequest = async (id: number) => {
    if (!window.confirm('Are you sure you want to delete this promotion request?')) {
      return;
    }
    
    try {
      await promotionService.deletePromotionRequest(id);
      fetchPromotionRequests();
      alert('Promotion request deleted successfully');
    } catch (error: any) {
      console.error('Failed to delete promotion request:', error);
      alert(error.response?.data?.message || 'Failed to delete promotion request');
    }
  };

  const handleReturnForRevision = async (id: number) => {
    setSelectedRequest(promotionRequests.find(r => r.id === id) || null);
    setOpenCommentDialog(true);
  };

  const handleSubmitReturnForRevision = async () => {
    if (!selectedRequest) return;
    
    if (!hrComment.trim()) {
      alert('Comment is required when returning for revision');
      return;
    }
    
    try {
      await promotionService.updateStatus(selectedRequest.id, 'returned_for_revision', hrComment);
      fetchPromotionRequests();
      setOpenCommentDialog(false);
      setHrComment('');
      handleCloseDetailDialog();
      alert('Request returned for revision');
    } catch (error: any) {
      console.error('Failed to return request:', error);
      alert(error.response?.data?.message || 'Failed to return request');
    }
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedFile(null);
    setEditingRequest(null);
    setSelectedGoalsIds([]);
    setAttachedGoals([]);
    setAttachedFiles([]);
  };

  const handleOpenDetailDialog = (request: PromotionRequest) => {
    setSelectedRequest(request);
    fetchAttachedGoals(request.id);
    fetchAttachedFiles(request.id);
    setOpenDetailDialog(true);
  };

  const handleCloseDetailDialog = () => {
    setOpenDetailDialog(false);
    setSelectedRequest(null);
    setAttachedGoals([]);
    setAttachedFiles([]);
  };

  const handleOpenGoalsDialog = (request: PromotionRequest) => {
    setSelectedRequest(request);
    fetchAttachedGoals(request.id);
    setSelectedGoalsIds([]);
    setOpenGoalsDialog(true);
  };

  const handleCloseGoalsDialog = () => {
    setOpenGoalsDialog(false);
    setSelectedRequest(null);
    setSelectedGoalsIds([]);
    setAttachedGoals([]);
  };

  const handleAttachGoals = async () => {
    if (!selectedRequest || selectedGoalsIds.length === 0) {
      alert('Please select at least one goal');
      return;
    }

    try {
      await promotionService.attachGoalsToPromotionRequest(selectedRequest.id, selectedGoalsIds);
      alert('Goals attached successfully');
      handleCloseGoalsDialog();
      fetchPromotionRequests();
    } catch (error: any) {
      console.error('Failed to attach goals:', error);
      alert(error.response?.data?.message || 'Failed to attach goals');
    }
  };

  const handleDetachGoal = async (goalId: number) => {
    if (!selectedRequest || !window.confirm('Remove this goal from the promotion request?')) {
      return;
    }

    try {
      await promotionService.detachGoalFromPromotionRequest(selectedRequest.id, goalId);
      alert('Goal removed successfully');
      fetchAttachedGoals(selectedRequest.id);
      fetchPromotionRequests();
    } catch (error: any) {
      console.error('Failed to detach goal:', error);
      alert(error.response?.data?.message || 'Failed to remove goal');
    }
  };

  const handleDownloadFile = async (fileId: number) => {
    try {
      await promotionService.downloadFile(fileId);
    } catch (error: any) {
      console.error('Failed to download file:', error);
      alert(error.response?.data?.message || 'Failed to download file');
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSelectChange = (e: any) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleSubmit = async () => {
    // Validate required fields
    if (!formData.employeeId) {
      alert('Employee ID is required');
      return;
    }
    if (!formData.requestedGradeId) {
      alert('Please select a requested grade');
      return;
    }
    if (!formData.justification.trim()) {
      alert('Please enter a justification');
      return;
    }
    if (!formData.reviewPeriod.trim()) {
      alert('Please enter a review period');
      return;
    }

    try {
      if (editingRequest) {
        await promotionService.updatePromotionRequest(editingRequest.id, formData);
        
        // Attach goals if any selected
        if (selectedGoalsIds.length > 0) {
          await promotionService.attachGoalsToPromotionRequest(editingRequest.id, selectedGoalsIds);
        }
        
        // Upload file if selected
        if (selectedFile) {
          await promotionService.uploadFile(editingRequest.id, selectedFile);
        }
        
        alert('Promotion request updated successfully');
      } else {
        const newRequest = await promotionService.createPromotionRequest(formData);
        
        // Attach goals if any selected
        if (selectedGoalsIds.length > 0) {
          await promotionService.attachGoalsToPromotionRequest(newRequest.id, selectedGoalsIds);
        }
        
        // Upload file if selected
        if (selectedFile) {
          await promotionService.uploadFile(newRequest.id, selectedFile);
        }
        
        alert('Promotion request created successfully');
      }
      
      fetchPromotionRequests();
      handleCloseDialog();
      setSelectedFile(null);
      setSelectedGoalsIds([]);
    } catch (error: any) {
      console.error('Failed to save promotion request:', error);
      const errorMessage = error.response?.data?.message || error.message || 'Failed to save promotion request. Please check all fields are filled correctly.';
      alert(errorMessage);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'approved':
        return 'success';
      case 'pending':
        return 'warning';
      case 'returned_for_revision':
        return 'error';
      case 'under_review':
        return 'info';
      case 'rejected':
        return 'error';
      case 'calibration_completed':
        return 'default';
      default:
        return 'default';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="lg">
      {!user?.employeeId && (
        <Box sx={{ mb: 2 }}>
          <Alert severity="info">
            Session needs refresh. Please log out and log in again.
          </Alert>
        </Box>
      )}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4">Promotion Requests</Typography>
        {canCreateRequest && (
          <Button variant="contained" startIcon={<Add />} onClick={handleOpenDialog}>
            Create Promotion Request
          </Button>
        )}
      </Box>

      {isHR && (
        <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Grade</InputLabel>
            <Select
              value={gradeFilter}
              onChange={(e) => setGradeFilter(e.target.value)}
              label="Grade"
            >
              <MenuItem value="">All Grades</MenuItem>
              {grades.map((grade) => (
                <MenuItem key={grade.id} value={grade.id}>
                  {grade.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Review Period</InputLabel>
            <Select
              value={periodFilter}
              onChange={(e) => setPeriodFilter(e.target.value)}
              label="Review Period"
            >
              <MenuItem value="">All Periods</MenuItem>
              {uniquePeriods.map((period) => (
                <MenuItem key={period} value={period}>
                  {period}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <FormControl sx={{ minWidth: 200 }}>
            <InputLabel>Status</InputLabel>
            <Select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              label="Status"
            >
              <MenuItem value="">All Statuses</MenuItem>
              {uniqueStatuses.map((status) => (
                <MenuItem key={status} value={status}>
                  {status}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
      )}

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Employee</TableCell>
              <TableCell>Requested Grade</TableCell>
              <TableCell>Review Period</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Justification</TableCell>
              <TableCell>Evidence</TableCell>
              {canCreateRequest && <TableCell>HR Comment</TableCell>}
              <TableCell>Submitted By</TableCell>
              <TableCell>Created At</TableCell>
              {(isHR || canCreateRequest) && <TableCell>Actions</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredRequests.length === 0 ? (
              <TableRow>
                <TableCell colSpan={canCreateRequest ? 10 : (isHR ? 9 : 8)} align="center">
                  {gradeFilter || periodFilter || statusFilter ? 'No promotion requests match the filters.' : 'No promotion requests found.'}
                </TableCell>
              </TableRow>
            ) : (
              filteredRequests.map((request) => (
                <TableRow key={request.id}>
                  <TableCell>{request.employeeName || `Employee #${request.employeeId}`}</TableCell>
                  <TableCell>{request.requestedGradeName || `Grade #${request.requestedGradeId}`}</TableCell>
                  <TableCell>{request.reviewPeriod}</TableCell>
                  <TableCell>
                    <Chip 
                      label={request.status} 
                      color={getStatusColor(request.status)} 
                      size="small" 
                    />
                  </TableCell>
                  <TableCell>{request.justification}</TableCell>
                  <TableCell>{request.evidence}</TableCell>
                  {canCreateRequest && (
                    <TableCell>
                      {request.hrComment ? (
                        <Typography variant="body2" color="error" sx={{ fontStyle: 'italic' }}>
                          {request.hrComment.length > 50 
                            ? request.hrComment.substring(0, 50) + '...' 
                            : request.hrComment}
                        </Typography>
                      ) : (
                        <Typography variant="body2" color="text.secondary">-</Typography>
                      )}
                    </TableCell>
                  )}
                  <TableCell>{request.submittedByName || `User #${request.submittedById}`}</TableCell>
                  <TableCell>{format(new Date(request.createdAt), 'MMM dd, yyyy')}</TableCell>
                  {isHR && (
                    <TableCell>
                      <Button 
                        size="small" 
                        variant="outlined"
                        onClick={() => handleOpenDetailDialog(request)}
                      >
                        View Details
                      </Button>
                    </TableCell>
                  )}
                  {canCreateRequest && request.employeeId === user?.employeeId && 
                    (request.status.toLowerCase() === 'pending' || request.status.toLowerCase() === 'returned_for_revision') && (
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        <Button 
                          size="small" 
                          variant="outlined"
                          color="primary"
                          onClick={() => handleEditRequest(request)}
                        >
                          Edit
                        </Button>
                        <Button 
                          size="small" 
                          variant="outlined"
                          color="success"
                          onClick={() => handleOpenGoalsDialog(request)}
                        >
                          Attach Goals
                        </Button>
                        <Button 
                          size="small" 
                          variant="outlined"
                          color="error"
                          onClick={() => handleDeleteRequest(request.id)}
                        >
                          Delete
                        </Button>
                      </Box>
                    </TableCell>
                  )}
                  {canCreateRequest && !(request.employeeId === user?.employeeId && 
                    (request.status.toLowerCase() === 'pending' || request.status.toLowerCase() === 'returned_for_revision')) && !isHR && (
                    <TableCell>-</TableCell>
                  )}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {canCreateRequest && (
        <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="md" fullWidth>
          <DialogTitle>{editingRequest ? 'Edit Promotion Request' : 'Create New Promotion Request'}</DialogTitle>
          <DialogContent>
            <FormControl fullWidth margin="normal" required>
              <InputLabel>Requested Grade</InputLabel>
              <Select
                name="requestedGradeId"
                value={formData.requestedGradeId}
                onChange={handleSelectChange}
                label="Requested Grade"
              >
                {grades.map((grade) => (
                  <MenuItem key={grade.id} value={grade.id}>
                    {grade.name} - {grade.description}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              fullWidth
              label="Review Period"
              name="reviewPeriod"
              value={formData.reviewPeriod}
              onChange={handleChange}
              margin="normal"
              placeholder="e.g., Q4-2024, 2024"
              required
            />
            <TextField
              fullWidth
              label="Justification"
              name="justification"
              value={formData.justification}
              onChange={handleChange}
              margin="normal"
              multiline
              rows={4}
              placeholder="Explain why you deserve this promotion"
              required
            />
            <TextField
              fullWidth
              label="Evidence (optional)"
              name="evidence"
              value={formData.evidence}
              onChange={handleChange}
              margin="normal"
              multiline
              rows={4}
              placeholder="Provide evidence of your achievements, completed goals, positive reviews, etc."
            />
            
            <FormControl fullWidth margin="normal">
              <InputLabel>Attach Completed Goals (optional)</InputLabel>
              <Select
                multiple
                value={selectedGoalsIds}
                onChange={(e) => setSelectedGoalsIds(e.target.value as number[])}
                label="Attach Completed Goals (optional)"
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {(selected as number[]).map((id) => {
                      const goal = completedGoals.find(g => g.id === id);
                      return goal ? (
                        <Chip key={id} label={goal.goalTitle} size="small" />
                      ) : null;
                    })}
                  </Box>
                )}
              >
                {completedGoals.length === 0 ? (
                  <MenuItem disabled>No completed goals available</MenuItem>
                ) : (
                  completedGoals.map((goal) => (
                    <MenuItem key={goal.id} value={goal.id}>
                      {goal.goalTitle} - {goal.goalMetric}
                    </MenuItem>
                  ))
                )}
              </Select>
              {completedGoals.length > 0 && (
                <Typography variant="caption" sx={{ mt: 1, color: 'text.secondary' }}>
                  You can attach completed goals to strengthen your promotion request
                </Typography>
              )}
            </FormControl>

            {/* Show already uploaded files in edit mode */}
            {editingRequest && attachedFiles.length > 0 && (
              <Box sx={{ mt: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  Previously Uploaded Files:
                </Typography>
                {attachedFiles.map((file) => (
                  <Paper key={file.id} sx={{ p: 1, mb: 1, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <Typography variant="body2">{file.fileName}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        ({(file.fileSize / 1024).toFixed(2)} KB)
                      </Typography>
                    </Box>
                    <Box>
                      <Button
                        size="small"
                        onClick={() => handleDownloadFile(file.id)}
                      >
                        Download
                      </Button>
                      <Button
                        size="small"
                        color="error"
                        onClick={async () => {
                          try {
                            await promotionService.deleteFile(file.id);
                            setAttachedFiles(attachedFiles.filter(f => f.id !== file.id));
                            alert('File deleted successfully');
                          } catch (error) {
                            console.error('Failed to delete file:', error);
                            alert('Failed to delete file');
                          }
                        }}
                      >
                        Delete
                      </Button>
                    </Box>
                  </Paper>
                ))}
              </Box>
            )}

            <Box sx={{ mt: 2 }}>
              <Button
                variant="outlined"
                component="label"
                fullWidth
              >
                {selectedFile ? `Selected: ${selectedFile.name}` : 'Upload Evidence File (optional)'}
                <input
                  type="file"
                  hidden
                  onChange={handleFileChange}
                  accept=".pdf,.doc,.docx,.txt,.png,.jpg,.jpeg"
                />
              </Button>
              {selectedFile && (
                <Typography variant="caption" display="block" sx={{ mt: 1, color: 'text.secondary' }}>
                  File size: {(selectedFile.size / 1024).toFixed(2)} KB
                </Typography>
              )}
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleCloseDialog}>Cancel</Button>
            <Button onClick={handleSubmit} variant="contained" color="primary">
              {editingRequest ? 'Update' : 'Create'}
            </Button>
          </DialogActions>
        </Dialog>
      )}

      {/* Detail Dialog for HR */}
      {isHR && selectedRequest && (
        <Dialog open={openDetailDialog} onClose={handleCloseDetailDialog} maxWidth="md" fullWidth>
          <DialogTitle>Promotion Request Details</DialogTitle>
          <DialogContent>
            <Box sx={{ mt: 2 }}>
              <Typography variant="h6" gutterBottom>Employee Information</Typography>
              <Typography><strong>Employee:</strong> {selectedRequest.employeeName}</Typography>
              <Typography><strong>Submitted By:</strong> {selectedRequest.submittedByName}</Typography>
              <Typography><strong>Created At:</strong> {format(new Date(selectedRequest.createdAt), 'MMMM dd, yyyy HH:mm')}</Typography>
              
              <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>Request Details</Typography>
              <Typography><strong>Requested Grade:</strong> {selectedRequest.requestedGradeName}</Typography>
              <Typography><strong>Review Period:</strong> {selectedRequest.reviewPeriod}</Typography>
              <Typography><strong>Status:</strong> <Chip label={selectedRequest.status} color={getStatusColor(selectedRequest.status)} size="small" /></Typography>
              
              <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>Justification</Typography>
              <Paper sx={{ p: 2, bgcolor: 'grey.50' }}>
                <Typography style={{ whiteSpace: 'pre-wrap' }}>{selectedRequest.justification}</Typography>
              </Paper>
              
              <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>Evidence</Typography>
              <Paper sx={{ p: 2, bgcolor: 'grey.50' }}>
                <Typography style={{ whiteSpace: 'pre-wrap' }}>{selectedRequest.evidence || 'No evidence provided'}</Typography>
              </Paper>
              
              <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>Attached Goals</Typography>
              {attachedGoals.length > 0 ? (
                <Box sx={{ mt: 1 }}>
                  {attachedGoals.map((goal) => (
                    <Chip
                      key={goal.id}
                      label={goal.goalTitle}
                      sx={{ mr: 1, mb: 1 }}
                      color="success"
                      variant="outlined"
                    />
                  ))}
                </Box>
              ) : (
                <Typography color="text.secondary">No goals attached</Typography>
              )}
              
              <Typography variant="h6" gutterBottom sx={{ mt: 3 }}>Attached Files</Typography>
              {attachedFiles.length > 0 ? (
                <Box sx={{ mt: 1 }}>
                  {attachedFiles.map((file) => (
                    <Paper key={file.id} sx={{ p: 2, mb: 1, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Typography variant="body1">{file.fileName}</Typography>
                        <Typography variant="caption" color="text.secondary">
                          ({(file.fileSize / 1024).toFixed(2)} KB)
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          • {new Date(file.uploadedAt).toLocaleDateString()}
                        </Typography>
                      </Box>
                      <Button
                        variant="contained"
                        size="small"
                        onClick={() => handleDownloadFile(file.id)}
                      >
                        Download
                      </Button>
                    </Paper>
                  ))}
                </Box>
              ) : (
                <Typography color="text.secondary">No files attached</Typography>
              )}
              
              {selectedRequest.hrComment && (
                <>
                  <Typography variant="h6" gutterBottom sx={{ mt: 3, color: 'error.main' }}>HR Comment</Typography>
                  <Paper sx={{ p: 2, bgcolor: 'error.lighter', borderLeft: 3, borderColor: 'error.main' }}>
                    <Typography style={{ whiteSpace: 'pre-wrap' }}>{selectedRequest.hrComment}</Typography>
                  </Paper>
                </>
              )}
            </Box>
          </DialogContent>
          <DialogActions>
            {selectedRequest.status.toLowerCase() === 'pending' && (
              <Button 
                onClick={() => handleReturnForRevision(selectedRequest.id)}
                variant="outlined"
                color="warning"
              >
                Return for Revision
              </Button>
            )}
            <Button onClick={handleCloseDetailDialog}>Close</Button>
          </DialogActions>
        </Dialog>
      )}

      {/* Comment Dialog for Return for Revision */}
      <Dialog open={openCommentDialog} onClose={() => setOpenCommentDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Return for Revision</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Please provide a comment explaining what needs to be revised:
            </Typography>
            <TextField
              fullWidth
              multiline
              rows={4}
              value={hrComment}
              onChange={(e) => setHrComment(e.target.value)}
              placeholder="Explain what needs to be changed..."
              required
              sx={{ mt: 2 }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setOpenCommentDialog(false); setHrComment(''); }}>Cancel</Button>
          <Button onClick={handleSubmitReturnForRevision} variant="contained" color="warning">
            Return for Revision
          </Button>
        </DialogActions>
      </Dialog>

      {/* Attach Goals Dialog */}
      <Dialog open={openGoalsDialog} onClose={handleCloseGoalsDialog} maxWidth="md" fullWidth>
        <DialogTitle>Attach Completed Goals</DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Select your completed goals to attach as evidence for this promotion request:
            </Typography>
            
            {attachedGoals.length > 0 && (
              <Box sx={{ mt: 2, mb: 2 }}>
                <Typography variant="subtitle2" gutterBottom>Currently Attached:</Typography>
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                  {attachedGoals.map((goal) => (
                    <Chip
                      key={goal.id}
                      label={goal.goalTitle}
                      onDelete={() => handleDetachGoal(goal.id)}
                      color="success"
                      variant="outlined"
                    />
                  ))}
                </Box>
              </Box>
            )}

            {completedGoals.length > 0 ? (
              <FormControl fullWidth sx={{ mt: 2 }}>
                <InputLabel>Select Goals</InputLabel>
                <Select
                  multiple
                  value={selectedGoalsIds}
                  onChange={(e) => setSelectedGoalsIds(e.target.value as number[])}
                  label="Select Goals"
                  renderValue={(selected) => 
                    `${selected.length} goal(s) selected`
                  }
                >
                  {completedGoals
                    .filter(g => !attachedGoals.some(ag => ag.id === g.id))
                    .map((goal) => (
                      <MenuItem key={goal.id} value={goal.id}>
                        <Box>
                          <Typography variant="body1">{goal.goalTitle}</Typography>
                          <Typography variant="caption" color="text.secondary">
                            {goal.goalMetric} • Due: {format(new Date(goal.dueDate), 'MMM dd, yyyy')}
                          </Typography>
                        </Box>
                      </MenuItem>
                    ))}
                </Select>
              </FormControl>
            ) : (
              <Alert severity="info" sx={{ mt: 2 }}>
                No completed goals available. Complete some goals first to attach them as evidence.
              </Alert>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseGoalsDialog}>Cancel</Button>
          <Button 
            onClick={handleAttachGoals} 
            variant="contained" 
            color="primary"
            disabled={selectedGoalsIds.length === 0}
          >
            Attach Selected Goals
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default PromotionRequestPage;
